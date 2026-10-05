package study.users

import io.ktor.server.websocket.*
import io.ktor.websocket.*

class UserConnection(
    val uid : UserId,
    val session : WebSocketServerSession
)