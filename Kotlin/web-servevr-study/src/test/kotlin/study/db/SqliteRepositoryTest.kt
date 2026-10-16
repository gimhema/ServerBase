package study.db

import kotlinx.coroutines.runBlocking
import study.users.UserCredential
import study.users.UserId
import study.users.UserProfile
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqliteRepositoryTest {

    // 테스트마다 새 DB 파일 — 서로 데이터가 섞이지 않게
    private val dir = Files.createTempDirectory("sqlite-test").toFile()
    private val db = Database(dir.resolve("test.db").path).apply { createSchema() }
    private val credentials = SqliteCredentialRepository(db)
    private val profiles = SqliteProfileRepository(db)

    @AfterTest
    fun cleanup() {
        dir.deleteRecursively()
    }

    private fun credential(loginId: String = "kimmo") = UserCredential(UserId.generate(), loginId, "\$2a\$04\$hash")

    @Test
    fun `saved credential can be found by loginId`() = runBlocking {
        val saved = credential()
        assertTrue(credentials.saveIfAbsent(saved))
        assertEquals(saved, credentials.findByLoginId("kimmo"))
        assertNull(credentials.findByLoginId("nobody"))
    }

    @Test
    fun `duplicate loginId is not saved`() = runBlocking {
        val first = credential()
        assertTrue(credentials.saveIfAbsent(first))
        assertFalse(credentials.saveIfAbsent(credential()))
        assertEquals(first.uid, credentials.findByLoginId("kimmo")?.uid)
    }

    @Test
    fun `sql in loginId is treated as plain value`() = runBlocking {
        credentials.saveIfAbsent(credential())
        assertNull(credentials.findByLoginId("' OR '1'='1"))
    }

    @Test
    fun `profile is inserted then updated`() = runBlocking {
        val credential = credential()
        credentials.saveIfAbsent(credential)

        profiles.save(UserProfile(credential.uid, "킴모"))
        profiles.save(UserProfile(credential.uid, "킴모2", "소개"))

        assertEquals(UserProfile(credential.uid, "킴모2", "소개"), profiles.findByUid(credential.uid))
    }

    @Test
    fun `profile without credential is rejected by foreign key`() = runBlocking {
        val result = runCatching { profiles.save(UserProfile(UserId.generate(), "주인없음")) }
        assertTrue(result.isFailure)
    }

    @Test
    fun `data survives reopening the file`() = runBlocking {
        val saved = credential()
        credentials.saveIfAbsent(saved)

        val reopened = SqliteCredentialRepository(Database(dir.resolve("test.db").path))
        assertEquals(saved, reopened.findByLoginId("kimmo"))
    }
}
