package study

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import study.db.Database
import study.db.SqliteCredentialRepository
import study.db.SqliteProfileRepository
import study.step1.echoRoute
import study.step2.tickerRoute
import study.step3.ChatRoom
import study.step3.chatRoute
import study.users.*
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = 15.seconds // 주기적으로 Ping 프레임 전송
        timeout = 15.seconds    // Pong 응답이 없으면 연결 종료
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(ContentNegotiation) {
        json() // HTTP 요청/응답 본문을 JSON <-> @Serializable 클래스로 자동 변환
    }

    val chatRoom = ChatRoom()

    // 저장소 — 다른 DB로 옮길 때는 이 부분만 교체
    val database = Database(System.getenv("USER_DB_PATH") ?: "data/users.db").apply { createSchema() }
    val credentials = SqliteCredentialRepository(database)
    val profiles = SqliteProfileRepository(database)

    // 서비스
    val hasher = BcryptPasswordHasher()
    val authenticator = Authenticator(credentials, hasher)
    val userRegister = UserRegister(credentials, profiles, hasher)
    val sessions = SessionStore()

    routing {
        staticResources("/", "static") // http://localhost:8080 → 브라우저 테스트 클라이언트
        echoRoute()
        tickerRoute()
        chatRoute(chatRoom)
        registerRoute(userRegister)
        loginRoute(authenticator, sessions)
    }
}
