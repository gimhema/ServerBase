package study

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import study.users.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class AuthRouteTest {

    // module()과 같은 구성이지만, 테스트 속도를 위해 bcrypt cost를 낮추고 SessionStore를 밖에서 확인할 수 있게 함
    private fun authTest(block: suspend ApplicationTestBuilder.(SessionStore) -> Unit) = testApplication {
        val credentials = InMemoryCredentialRepository()
        val hasher = BcryptPasswordHasher(cost = 4)
        val sessions = SessionStore()
        application { authModule(credentials, hasher, sessions) }
        block(sessions)
    }

    private fun Application.authModule(credentials: CredentialRepository, hasher: PasswordHasher, sessions: SessionStore) {
        install(ContentNegotiation) { json() }
        routing {
            registerRoute(UserRegister(credentials, InMemoryProfileRepository(), hasher))
            loginRoute(Authenticator(credentials, hasher), sessions)
        }
    }

    private suspend fun ApplicationTestBuilder.postJson(path: String, body: String) =
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend fun ApplicationTestBuilder.register(loginId: String = "kimmo", password: String = "password123") =
        postJson("/auth/register", """{"loginId":"$loginId","password":"$password","userName":"킴모"}""")

    private suspend fun ApplicationTestBuilder.login(loginId: String = "kimmo", password: String = "password123") =
        postJson("/auth/login", """{"loginId":"$loginId","loginPassword":"$password"}""")

    private suspend inline fun <reified T> HttpResponse.body(): T = Json.decodeFromString(bodyAsText())

    @Test
    fun `register then login issues token mapped to same uid`() = authTest { sessions ->
        val registered = register()
        assertEquals(HttpStatusCode.Created, registered.status)
        val uid = registered.body<RegisterResponse>().uid

        val loggedIn = login()
        assertEquals(HttpStatusCode.OK, loggedIn.status)
        val token = loggedIn.body<LoginResponse>().token

        assertEquals(uid, sessions.resolve(token)?.value?.toString())
    }

    @Test
    fun `duplicate loginId is rejected`() = authTest {
        register()
        val second = register()
        assertEquals(HttpStatusCode.Conflict, second.status)
        assertEquals("DUPLICATE_LOGIN_ID", second.body<ErrorResponse>().code)
    }

    @Test
    fun `invalid input is rejected`() = authTest {
        assertEquals(HttpStatusCode.BadRequest, register(loginId = "ab").status)            // 너무 짧은 id
        assertEquals(HttpStatusCode.BadRequest, register(password = "short").status)        // 너무 짧은 비밀번호
        assertEquals(HttpStatusCode.BadRequest, register(password = "가".repeat(25)).status) // 75바이트 > 72
    }

    @Test
    fun `wrong password and unknown loginId get same 401`() = authTest {
        register()
        val wrongPassword = login(password = "wrong-password")
        val unknownId = login(loginId = "nobody")

        assertEquals(HttpStatusCode.Unauthorized, wrongPassword.status)
        assertEquals(HttpStatusCode.Unauthorized, unknownId.status)
        assertEquals(wrongPassword.bodyAsText(), unknownId.bodyAsText())
    }

    @Test
    fun `malformed json is 400`() = authTest {
        assertEquals(HttpStatusCode.BadRequest, postJson("/auth/register", "{not json").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/auth/login", """{"loginId":"kimmo"}""").status)
    }

    @Test
    fun `session expires after ttl and can be revoked`() {
        var now = Instant.parse("2026-01-01T00:00:00Z")
        val clock = object : Clock() {
            override fun instant() = now
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?) = this
        }
        val sessions = SessionStore(ttl = 1.hours, clock = clock)
        val uid = UserId.generate()

        val token = sessions.issue(uid)
        now = now.plus(59.minutes.toJavaDuration())
        assertEquals(uid, sessions.resolve(token))
        now = now.plus(1.minutes.toJavaDuration())
        assertNull(sessions.resolve(token))

        val another = sessions.issue(uid)
        assertNotNull(sessions.resolve(another))
        sessions.revoke(another)
        assertNull(sessions.resolve(another))
    }
}
