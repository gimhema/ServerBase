package study.users

import kotlinx.serialization.Serializable

enum class Role { USER, ADMIN }

data class UserRole(
    val uid : UserId,
    val role : Set<Role>
)