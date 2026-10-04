package study.users

import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate() = UserId(UUID.randomUUID())
    }
}

data class UserAccountInfo(
    val Id : String,
    val Password : String,
    val UID : UserId
)

data class UserProfile(
    val userName : String,
    val description : String
)

class User(
    val userAccountInfo : UserAccountInfo,
    val userProfile : UserProfile
)


class UserFactory(
    
)

class UserManager(
    
)