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
data class RegisterResponse(
    val uid: String
)

/**
 * POST /auth/register
 * 요청: {"loginId": "...", "password": "...", "userName": "..."}
 * 응답: 201 {"uid"} / 400 INVALID_INPUT / 409 DUPLICATE_LOGIN_ID
 *
 * Route는 요청 파싱과 결과 → HTTP 응답 변환만 담당하고, 가입 로직은 UserRegister에 둠
 */
fun Route.registerRoute(register: UserRegister) {
    post("/auth/register") {
        val request = call.receive<RegisterRequest>()  // JSON 형식이 틀리면 400

        // bcrypt 해싱은 CPU를 오래 쓰므로 요청 처리 스레드를 막지 않도록 계산용 디스패처에서 실행
        val result = withContext(Dispatchers.Default) { register.register(request) }

        when (result) {
            is RegisterResult.Success ->
                call.respond(HttpStatusCode.Created, RegisterResponse(result.uid.value.toString()))
            RegisterResult.DuplicateLoginId ->
                call.respond(HttpStatusCode.Conflict, ErrorResponse("DUPLICATE_LOGIN_ID", "이미 사용 중인 loginId"))
            is RegisterResult.InvalidInput ->
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_INPUT", result.reason))
        }
    }
}
