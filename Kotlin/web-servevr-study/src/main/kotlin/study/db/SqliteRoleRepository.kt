package study.db

import study.users.Role
import study.users.RoleRepository
import study.users.UserId
import study.users.UserProfile
import study.users.UserRole
import java.sql.ResultSet
import java.util.UUID


class SqliteRoleRepository(private val db: Database) : RoleRepository {
override suspend fun findByUid(uid: UserId): UserRole? = db.withConnection { conn ->
    conn.prepareStatement("SELECT role FROM user_roles WHERE uid = ?").use { stmt ->
        stmt.setString(1, uid.value.toString())
        stmt.executeQuery().use { rs ->
            val roles = mutableSetOf<Role>()
            while (rs.next()) {
                roles += Role.valueOf(rs.getString("role"))
            }
            UserRole(uid, roles)
        }
    }
}

    override suspend fun grant(uid: UserId, role: Role) {
        db.withConnection { conn ->
            conn.prepareStatement(
                """
                INSERT INTO user_roles (uid, role) VALUES (?, ?) ON CONFLICT DO NOTHING
                """
            ).use { stmt ->
                stmt.setString(1, uid.value.toString())
                stmt.setString(2, role.toString())
                stmt.executeUpdate()
            }
        }
    }

    override suspend fun revoke(uid: UserId, role: Role) {
        db.withConnection { conn ->
            conn.prepareStatement(
                """
                DELETE FROM user_roles WHERE uid = (?) and role = (?)
                """
            ).use { stmt ->
                stmt.setString(1, uid.value.toString())
                stmt.setString(2, role.toString())
                stmt.executeUpdate()
            }
        }
    }
}