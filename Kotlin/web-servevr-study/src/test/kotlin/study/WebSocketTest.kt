package study

import io.ktor.client.plugins.websocket.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketTest {

    private suspend fun ReceiveChannel<Frame>.nextText(): String = (receive() as Frame.Text).readText()

    private fun ApplicationTestBuilder.wsClient() = createClient { install(WebSockets) }

    @Test
    fun `step1 echo`() = testApplication {
        application { module() }
        wsClient().webSocket("/ws/echo") {
            assertEquals("connected", incoming.nextText())
            send("hello")
            assertEquals("echo: hello", incoming.nextText())
        }
    }

    @Test
    fun `step2 ticker changes interval`() = testApplication {
        application { module() }
        wsClient().webSocket("/ws/ticker") {
            assertEquals("tick 0 (every 1000ms)", incoming.nextText())
            send("100")
            assertEquals("tick 1 (every 100ms)", incoming.nextText())
            assertEquals("tick 2 (every 100ms)", incoming.nextText())
        }
    }

    @Test
    fun `step3 chat broadcasts between sessions`() = testApplication {
        application { module() }
        val client = wsClient()
        client.webSocket("/ws/chat?name=alice") {
            val alice = this
            assertTrue(alice.incoming.nextText().startsWith("[system] alice joined"))

            client.webSocket("/ws/chat?name=bob") {
                // bob 은 replay 로 alice 입장 메시지부터 받는다
                assertTrue(incoming.nextText().startsWith("[system] alice joined"))
                assertTrue(incoming.nextText().startsWith("[system] bob joined"))
                assertTrue(alice.incoming.nextText().startsWith("[system] bob joined"))

                send("hi alice")
                assertEquals("bob: hi alice", alice.incoming.nextText())
                assertEquals("bob: hi alice", incoming.nextText())
            }
        }
    }
}
