package study.users

import java.util.concurrent.ConcurrentHashMap

// 테스트 전용 메모리 구현 — 서비스 코드는 study.db의 SQLite 구현을 사용

class InMemoryCredentialRepository : CredentialRepository {
    private val byLoginId = ConcurrentHashMap<String, UserCredential>()

    override suspend fun findByLoginId(loginId: String): UserCredential? = byLoginId[loginId]

    override suspend fun saveIfAbsent(credential: UserCredential): Boolean =
        byLoginId.putIfAbsent(credential.loginId, credential) == null
}

class InMemoryProfileRepository : ProfileRepository {
    private val byUid = ConcurrentHashMap<UserId, UserProfile>()

    override suspend fun findByUid(uid: UserId): UserProfile? = byUid[uid]

    override suspend fun save(profile: UserProfile) {
        byUid[profile.uid] = profile
    }
}
