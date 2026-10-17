package study.users

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val loginId : String,
    val loginPassword : String
)

data class UserCredential(
    val uid : UserId,
    val loginId : String,
    val passwordHash : String
)


interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun matches(rawPassword: String, passwordHash: String): Boolean
}

// cost: 2^cost 번 반복 — 1 올릴 때마다 계산 시간 약 2배
class BcryptPasswordHasher(
    private val cost: Int = 12
) : PasswordHasher {
    override fun hash(rawPassword: String): String =
        BCrypt.withDefaults().hashToString(cost, rawPassword.toCharArray())

    // 솔트와 cost는 passwordHash 안에 들어 있어서 그대로 꺼내 다시 계산함
    override fun matches(rawPassword: String, passwordHash: String): Boolean =
        BCrypt.verifyer().verify(rawPassword.toCharArray(), passwordHash).verified
}


class Authenticator(
    private val credentials: CredentialRepository,  // loginId로 UserCredential 조회
    private val hasher: PasswordHasher,             // 비밀번호 해시 비교
) {
    // 없는 loginId일 때도 해시 비교를 한 번 수행하기 위한 더미 값
    private val dummyHash by lazy { hasher.hash("dummy-password") }

    suspend fun authenticate(request: LoginRequest): UserId? {
        val credential = credentials.findByLoginId(request.loginId)
        if (credential == null) {
            // 바로 return하면 응답이 눈에 띄게 빨라져서, 응답 시간만으로 가입된 아이디인지 알아낼 수 있음
            hasher.matches(request.loginPassword, dummyHash)
            return null
        }
        if (!hasher.matches(request.loginPassword, credential.passwordHash)) return null
        return credential.uid
    }
}
