package study

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import study.step1.echoRoute
import study.step2.tickerRoute
import study.step3.ChatRoom
import study.step3.chatRoute
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = 15.seconds // 주기적으로 Ping 프레임 전송
        timeout = 15.seconds    // Pong 응답이 없으면 연결 종료
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val chatRoom = ChatRoom()

    routing {
        staticResources("/", "static") // http://localhost:8080 → 브라우저 테스트 클라이언트
        echoRoute()
        tickerRoute()
        chatRoute(chatRoom)
    }
}
