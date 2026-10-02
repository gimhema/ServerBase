package study.step3

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * 하나의 채팅방. 모든 세션이 같은 SharedFlow 를 구독해 메시지를 브로드캐스트 받는다.
 *
 * - replay: 새로 들어온 사용자에게 최근 메시지를 다시 보내준다.
 * - DROP_OLDEST: 느린 구독자 때문에 발행자(post)가 suspend 되지 않도록 한다.
 *   (SUSPEND 로 바꾸면 느린 클라이언트 하나가 방 전체를 막는 것을 직접 실험해볼 수 있다.)
 */
class ChatRoom(replay: Int = 20) {
    private val _messages = MutableSharedFlow<String>(
        replay = replay,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val members = AtomicInteger()
    val memberCount: Int get() = members.get()

    fun join(name: String) {
        members.incrementAndGet()
        post("[system] $name joined (${memberCount} online)")
    }

    fun leave(name: String) {
        members.decrementAndGet()
        post("[system] $name left (${memberCount} online)")
    }

    fun say(name: String, text: String) = post("$name: $text")

    // DROP_OLDEST 이므로 tryEmit 은 항상 성공한다. suspend 가 아니라서 finally 블록에서도 안전하게 호출 가능.
    private fun post(message: String) {
        _messages.tryEmit(message)
    }
}
