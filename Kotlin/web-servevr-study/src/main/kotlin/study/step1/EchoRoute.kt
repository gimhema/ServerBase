package study.step1

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

/**
 * Step 1. 웹소켓 기초 — 받은 텍스트를 그대로 돌려준다.
 *
 * - webSocket { } 블록 자체가 suspend 람다이며, 블록이 끝나면 연결이 닫힌다.
 * - incoming 은 ReceiveChannel<Frame>. for 루프는 클라이언트가 연결을 닫으면 종료된다.
 */
fun Route.echoRoute() {
    webSocket("/ws/echo") {
        send("connected")
        for (frame in incoming) {
            if (frame !is Frame.Text) continue
            val text = frame.readText()
            if (text.equals("bye", ignoreCase = true)) {
                close(CloseReason(CloseReason.Codes.NORMAL, "client said bye"))
                break
            }
            send("echo: $text")
        }
    }
}
