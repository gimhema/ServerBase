package study.users

import java.util.concurrent.ConcurrentHashMap


data class LoginRequest(
    val loginId : String,
    val loginPassword : String
)

data class UserCredential(
    val uid : UserId,
    val loginId : String,
    val passwordHash : String
)


interface CredentialRepository {
    fun findByLoginId(loginId: String): UserCredential?
    fun save(credential: UserCredential)
}

// DB 구축 전 테스트용 — 서버 재시작 시 데이터 사라짐
class InMemoryCredentialRepository : CredentialRepository {
    private val byLoginId = ConcurrentHashMap<String, UserCredential>()

    override fun findByLoginId(loginId: String): UserCredential? = byLoginId[loginId]

    override fun save(credential: UserCredential) {
        // 이미 있는 loginId면 덮어쓰지 않고 실패시킴 (동시 가입 시 중복 방지)
        check(byLoginId.putIfAbsent(credential.loginId, credential) == null) {
            "이미 존재하는 loginId: ${credential.loginId}"
        }
    }
}


interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun matches(rawPassword: String, passwordHash: String): Boolean
}


class Authenticator(
    private val credentials: CredentialRepository,  // loginId로 UserCredential 조회
    private val hasher: PasswordHasher,             // 비밀번호 해시 비교
) {
    fun authenticate(request: LoginRequest): UserId? {
        val credential = credentials.findByLoginId(request.loginId) ?: return null
        if (!hasher.matches(request.loginPassword, credential.passwordHash)) return null
        return credential.uid
    }
}