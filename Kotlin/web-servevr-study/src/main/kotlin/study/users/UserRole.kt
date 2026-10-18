package study.users

import kotlinx.serialization.Serializable

enum class Role { USER, ADMIN }

interface RoleRepository {
    suspend fun rolesOf(uid : UserId) : Set<Role>
    suspend fun grant(uid: UserId, role : Role)
    suspend fun revoke(uid: UserId, role : Role)
}