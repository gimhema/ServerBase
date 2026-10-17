package study.users

import java.util.UUID

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
