package study.users

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import study.ErrorResponse


@Serializable
data class LoginResponse(
    val token: String,
    val expiresInSeconds: Long
)

/**
 * POST /auth/login
 * 요청: {"loginId": "...", "loginPassword": "..."}
 * 응답: 200 {"token", "expiresInSeconds"} / 401 INVALID_CREDENTIALS
 *
 * 발급된 token은 이후 WebSocket 연결 등에서 SessionStore.resolve()로 UserId를 확인할 때 사용
 */
fun Route.loginRoute(authenticator: Authenticator, sessions: SessionStore) {
    post("/auth/login") {
        val request = call.receive<LoginRequest>()

        val uid = withContext(Dispatchers.Default) { authenticator.authenticate(request) }

        if (uid == null) {
            // 아이디가 없는지 비밀번호가 틀렸는지 구분해서 알려주면 가입된 아이디를 알아낼 수 있으므로 같은 응답
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("INVALID_CREDENTIALS", "loginId 또는 password가 올바르지 않음"))
            return@post
        }

        call.respond(LoginResponse(sessions.issue(uid), sessions.ttl.inWholeSeconds))
    }
}
