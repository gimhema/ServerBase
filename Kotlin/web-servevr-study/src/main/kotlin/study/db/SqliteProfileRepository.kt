package study.db

import study.users.ProfileRepository
import study.users.UserId
import study.users.UserProfile
import java.sql.ResultSet
import java.util.UUID

class SqliteProfileRepository(private val db: Database) : ProfileRepository {

    override suspend fun findByUid(uid: UserId): UserProfile? = db.withConnection { conn ->
        conn.prepareStatement("SELECT uid, user_name, description FROM profiles WHERE uid = ?").use { stmt ->
            stmt.setString(1, uid.value.toString())
            stmt.executeQuery().use { rs -> if (rs.next()) rs.toProfile() else null }
        }
    }

    // 없으면 추가, 있으면 갱신 (메모리 구현의 map[uid] = profile과 같은 동작)
    override suspend fun save(profile: UserProfile) {
        db.withConnection { conn ->
            conn.prepareStatement(
                """
                INSERT INTO profiles (uid, user_name, description) VALUES (?, ?, ?)
                ON CONFLICT(uid) DO UPDATE SET user_name = excluded.user_name, description = excluded.description
                """
            ).use { stmt ->
                stmt.setString(1, profile.uid.value.toString())
                stmt.setString(2, profile.userName)
                stmt.setString(3, profile.description)
                stmt.executeUpdate()
            }
        }
    }

    private fun ResultSet.toProfile() = UserProfile(
        uid = UserId(UUID.fromString(getString("uid"))),
        userName = getString("user_name"),
        description = getString("description"),
    )
}
