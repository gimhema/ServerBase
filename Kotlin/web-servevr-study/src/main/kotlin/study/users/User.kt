package study.users

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate() = UserId(UUID.randomUUID())
    }
}

// 순수 유저 정보 — 인증 정보(UserCredential)와는 UserId로만 연결됨. 나중에 DB 테이블과 1:1
data class UserProfile(
    val uid : UserId,
    val userName : String,
    val description : String = ""
)


interface ProfileRepository {
    fun findByUid(uid: UserId): UserProfile?
    fun save(profile: UserProfile)
}

// DB 구축 전 테스트용 — 서버 재시작 시 데이터 사라짐
class InMemoryProfileRepository : ProfileRepository {
    private val byUid = ConcurrentHashMap<UserId, UserProfile>()

    override fun findByUid(uid: UserId): UserProfile? = byUid[uid]

    override fun save(profile: UserProfile) {
        byUid[profile.uid] = profile
    }
}
