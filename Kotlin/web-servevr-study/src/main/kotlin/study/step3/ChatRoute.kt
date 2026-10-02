package study.step3

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch

/**
 * Step 3. 여러 세션 간 브로드캐스트 — ws://localhost:8080/ws/chat?name=alice
 *
 * 세션마다 두 개의 흐름이 동시에 돈다.
 *  - 수신: incoming 채널 → room.say()
 *  - 송신: room.messages(SharedFlow) 구독 → send()
 */
fun Route.chatRoute(room: ChatRoom) {
    webSocket("/ws/chat") {
        val name = call.request.queryParameters["name"]?.takeIf { it.isNotBlank() }
            ?: "guest-${(1000..9999).random()}"

        val subscriber = launch {
            room.messages.collect { send(it) }
        }
        room.join(name)

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) room.say(name, frame.readText())
            }
        } finally {
            room.leave(name)
            subscriber.cancel()
        }
    }
}
