package study.users

// users가 필요로 하는 저장소 인터페이스 모음
// 사용하는 쪽(users)이 정의하고, study.db가 구현함 — users는 db를 import하지 않음
// 테스트용 메모리 구현은 src/test의 InMemoryRepositories.kt


// 구현: study.db.SqliteCredentialRepository
interface CredentialRepository {
    suspend fun findByLoginId(loginId: String): UserCredential?

    // 같은 loginId가 이미 있으면 저장하지 않고 false — 중복 확인과 저장이 한 번에(원자적으로) 일어나야 함
    suspend fun saveIfAbsent(credential: UserCredential): Boolean
}

// 구현: study.db.SqliteProfileRepository
interface ProfileRepository {
    suspend fun findByUid(uid: UserId): UserProfile?
    suspend fun save(profile: UserProfile)
}

interface RoleRepository {
    suspend fun findByUid(uid : UserId) : UserRole?
    suspend fun grant(uid: UserId, role : Role)
    suspend fun revoke(uid: UserId, role : Role)
}