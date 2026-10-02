package study.step2

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Step 2. 세션 안에서 코루틴 다루기 — 서버가 주기적으로 push 하면서 동시에 클라이언트 입력도 받는다.
 *
 * - 웹소켓 세션은 CoroutineScope 이므로 launch 로 송신 전용 코루틴을 띄울 수 있다.
 * - 클라이언트가 숫자(ms)를 보내면 StateFlow 값이 바뀌고, collectLatest 가 이전 루프를 취소하고 새 주기로 재시작한다.
 * - 연결이 끝나면 finally 에서 송신 코루틴을 명시적으로 취소한다.
 */
fun Route.tickerRoute() {
    webSocket("/ws/ticker") {
        val interval = MutableStateFlow(1_000L)
        var count = 0

        val ticker = launch {
            interval.collectLatest { ms ->
                while (true) {
                    send("tick ${count++} (every ${ms}ms)")
                    delay(ms)
                }
            }
        }

        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val ms = frame.readText().trim().toLongOrNull()
                if (ms == null) {
                    send("error: send a number in ms")
                    continue
                }
                interval.value = ms.coerceAtLeast(100)
            }
        } finally {
            ticker.cancel()
            call.application.log.info("ticker session closed after $count ticks")
        }
    }
}
