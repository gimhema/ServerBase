package study.users

import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

// 로그인 성공 시 발급하는 토큰 → UserId 매핑
// 계정 정보가 아니라 휘발되어도 되는 접속 정보라서 메모리에 둠 (서버 재시작 시 다시 로그인하면 됨)
class SessionStore(
    val ttl: Duration = 24.hours,
    private val clock: Clock = Clock.systemUTC(),  // 테스트에서 시간을 조작할 수 있도록 주입
) {
    private class Session(val uid: UserId, val expiresAt: Instant)

    private val sessions = ConcurrentHashMap<String, Session>()
    private val random = SecureRandom()

    fun issue(uid: UserId): String {
        val token = newToken()
        sessions[token] = Session(uid, clock.instant().plus(ttl.toJavaDuration()))
        return token
    }

    // 없거나 만료된 토큰이면 null
    fun resolve(token: String): UserId? {
        val session = sessions[token] ?: return null
        if (!clock.instant().isBefore(session.expiresAt)) {
            sessions.remove(token, session)
            return null
        }
        return session.uid
    }

    fun revoke(token: String) {
        sessions.remove(token)
    }

    // 32바이트(256비트) 난수 — 추측 불가능해야 하므로 Random이 아닌 SecureRandom 사용
    private fun newToken(): String {
        val bytes = ByteArray(32).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
