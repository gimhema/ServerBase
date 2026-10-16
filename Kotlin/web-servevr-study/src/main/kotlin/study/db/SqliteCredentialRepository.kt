package study.db

import study.users.CredentialRepository
import study.users.UserCredential
import study.users.UserId
import java.sql.ResultSet
import java.util.UUID

// SQL에는 항상 ?만 쓰고 값은 setXxx()로 바인딩 — 입력값이 SQL 구문으로 해석되지 않음 (SQL 주입 방지)
class SqliteCredentialRepository(private val db: Database) : CredentialRepository {

    override suspend fun findByLoginId(loginId: String): UserCredential? = db.withConnection { conn ->
        conn.prepareStatement("SELECT uid, login_id, password_hash FROM credentials WHERE login_id = ?").use { stmt ->
            stmt.setString(1, loginId)
            stmt.executeQuery().use { rs -> if (rs.next()) rs.toCredential() else null }
        }
    }

    // ON CONFLICT DO NOTHING: 이미 있는 login_id면 아무것도 안 하고 0행 반영 → false
    // 확인과 저장이 SQL 한 문장이라 동시에 같은 아이디로 가입해도 하나만 성공함
    override suspend fun saveIfAbsent(credential: UserCredential): Boolean = db.withConnection { conn ->
        conn.prepareStatement(
            "INSERT INTO credentials (uid, login_id, password_hash) VALUES (?, ?, ?) ON CONFLICT(login_id) DO NOTHING"
        ).use { stmt ->
            stmt.setString(1, credential.uid.value.toString())
            stmt.setString(2, credential.loginId)
            stmt.setString(3, credential.passwordHash)
            stmt.executeUpdate() == 1
        }
    }

    // DB 행 → 도메인 객체 변환은 Repository 안에서만
    private fun ResultSet.toCredential() = UserCredential(
        uid = UserId(UUID.fromString(getString("uid"))),
        loginId = getString("login_id"),
        passwordHash = getString("password_hash"),
    )
}
