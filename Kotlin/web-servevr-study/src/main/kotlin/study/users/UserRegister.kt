package study.users

import kotlinx.serialization.Serializable


// UserId는 서버가 생성하므로 요청에 포함하지 않음
@Serializable
data class RegisterRequest(
    val loginId : String,
    val password : String,
    val userName : String
)

sealed interface RegisterResult {
    data class Success(val uid: UserId) : RegisterResult
    data object DuplicateLoginId : RegisterResult
    data class InvalidInput(val reason: String) : RegisterResult
}


class UserRegister(
    private val credentials: CredentialRepository,
    private val profiles: ProfileRepository,
    private val hasher: PasswordHasher,
) {
    fun register(request: RegisterRequest): RegisterResult {
        validate(request)?.let { return RegisterResult.InvalidInput(it) }

        // 해싱은 느리니까 명백한 중복은 먼저 걸러냄 (최종 판단은 아래 saveIfAbsent)
        if (credentials.findByLoginId(request.loginId) != null) return RegisterResult.DuplicateLoginId

        val uid = UserId.generate()
        val credential = UserCredential(uid, request.loginId, hasher.hash(request.password))

        // 위 확인 이후 같은 loginId로 동시에 가입한 요청이 먼저 저장했을 수 있음
        if (!credentials.saveIfAbsent(credential)) return RegisterResult.DuplicateLoginId

        profiles.save(UserProfile(uid, request.userName))
        return RegisterResult.Success(uid)
    }

    // 문제가 있으면 이유를, 없으면 null
    private fun validate(request: RegisterRequest): String? = when {
        !LOGIN_ID_PATTERN.matches(request.loginId) ->
            "loginId는 영문 소문자, 숫자, _ 조합 4~20자"
        request.password.length < MIN_PASSWORD_LENGTH ->
            "password는 ${MIN_PASSWORD_LENGTH}자 이상"
        // bcrypt는 72바이트까지만 처리함 (한글은 한 글자 3바이트)
        request.password.toByteArray().size > MAX_PASSWORD_BYTES ->
            "password가 너무 김 (최대 ${MAX_PASSWORD_BYTES}바이트)"
        request.userName.isBlank() || request.userName.length > MAX_USER_NAME_LENGTH ->
            "userName은 1~${MAX_USER_NAME_LENGTH}자"
        else -> null
    }

    companion object {
        private val LOGIN_ID_PATTERN = Regex("^[a-z0-9_]{4,20}$")
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_BYTES = 72
        private const val MAX_USER_NAME_LENGTH = 20
    }
}
