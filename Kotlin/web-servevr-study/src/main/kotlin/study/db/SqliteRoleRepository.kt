package study.db

import study.users.Role
import study.users.RoleRepository
import study.users.UserId
import study.users.UserProfile
import study.users.UserRole
import java.sql.ResultSet
import java.util.UUID


class SqliteRoleRepository(private val db: Database) : RoleRepository {
    override suspend fun findByUid(uid: UserId): UserRole? {
        TODO("Not yet implemented")
    }

    override suspend fun grant(uid: UserId, role: Role) {
        TODO("Not yet implemented")
    }

    override suspend fun revoke(uid: UserId, role: Role) {
        TODO("Not yet implemented")
    }
}