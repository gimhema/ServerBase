package study.db

// 테이블 정의 — DB에 "어떻게 저장되는지"만 담당, 도메인 객체(UserCredential, UserProfile)와는 별개
// UUID는 sqlite3 CLI로 들여다보기 쉽도록 TEXT로 저장
internal object Schema {
    val statements = listOf(
        """
        CREATE TABLE IF NOT EXISTS credentials (
            uid           TEXT PRIMARY KEY,
            login_id      TEXT NOT NULL UNIQUE,     -- 중복 가입은 최종적으로 DB 제약이 막음
            password_hash TEXT NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS profiles (
            uid         TEXT PRIMARY KEY REFERENCES credentials(uid) ON DELETE CASCADE,
            user_name   TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT ''
        )
        """,
    )
}
