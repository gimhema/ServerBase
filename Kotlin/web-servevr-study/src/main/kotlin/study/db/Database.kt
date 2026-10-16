package study.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File
import java.sql.Connection

/**
 * SQLite 연결 관리 — C++ 구조로 치면 DB 에이전트의 연결 관리 부분
 *
 * SQLite는 서버 없이 파일 하나로 동작하므로 연결을 여는 비용이 작아서, 작업마다 연결을 열고 닫음
 * (PostgreSQL 등으로 옮기면 이 자리를 커넥션 풀(HikariCP)로 교체)
 */
class Database(path: String) {
    private val dataSource = SQLiteDataSource(
        SQLiteConfig().apply {
            enforceForeignKeys(true)                        // SQLite는 기본값이 외래키 검사 꺼짐
            setJournalMode(SQLiteConfig.JournalMode.WAL)    // 읽기와 쓰기가 서로 덜 막히도록
            setBusyTimeout(5_000)                           // 다른 쓰기가 끝날 때까지 최대 5초 대기 후 실패
        }
    ).apply { url = "jdbc:sqlite:$path" }

    init {
        File(path).absoluteFile.parentFile?.mkdirs()
    }

    // JDBC는 블로킹 API라서 I/O용 디스패처에서 실행 — 호출하는 코루틴 스레드를 막지 않음
    suspend fun <T> withConnection(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use(block)
    }

    // 테이블이 없으면 생성 — 서버 시작 시 한 번만 호출하므로 블로킹으로 둠
    // 스키마가 자주 바뀌게 되면 Flyway 같은 마이그레이션 도구로 교체
    fun createSchema() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                Schema.statements.forEach(stmt::execute)
            }
        }
    }
}
