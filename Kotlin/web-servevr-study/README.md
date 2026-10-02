# Kotlin Web Server Study — WebSocket × Coroutine

Ktor 3 기반으로 **웹소켓**과 **코루틴**을 단계별로 익히는 스터디 프로젝트.

| 항목 | 버전 |
|---|---|
| Kotlin | 2.4.0 |
| Ktor | 3.6.0 (Netty 엔진) |
| JDK | 21 |
| Gradle | 9.7.1 |

## 실행

```bash
./gradlew run      # http://localhost:8080 에서 브라우저 테스트 클라이언트 사용
./gradlew test     # 단계별 웹소켓 테스트
```

CLI로 테스트하려면 [websocat](https://github.com/vi/websocat): `websocat ws://localhost:8080/ws/echo`

## 구조

```
src/main/kotlin/study/
├── Application.kt          # 서버 진입점, WebSockets 플러그인 설정
├── step1/EchoRoute.kt      # /ws/echo
├── step2/TickerRoute.kt    # /ws/ticker
└── step3/                  # /ws/chat?name=...
    ├── ChatRoom.kt
    └── ChatRoute.kt
src/main/resources/static/index.html   # 브라우저 테스트 클라이언트
src/test/kotlin/study/WebSocketTest.kt # testApplication 기반 테스트
```

## 로드맵

### ✅ Step 1. 웹소켓 기초 — Echo
- 핸드셰이크(HTTP 101 Switching Protocols)와 프레임(Text/Binary/Ping/Pong/Close)
- `webSocket { }` 블록 = 하나의 세션 = 하나의 코루틴
- `incoming: ReceiveChannel<Frame>` / `send()` 는 suspend 함수
- **실험**: `bye` 를 보내 서버 측 close, 브라우저에서 끊었을 때 for 루프가 어떻게 끝나는지

### ✅ Step 2. 세션 안의 동시성 — Ticker
- 세션은 `CoroutineScope` → `launch` 로 송신 코루틴을 분리
- `StateFlow` + `collectLatest` 로 실행 중인 루프를 취소하고 재시작
- `finally` 에서의 정리, 취소(cancellation)의 전파
- **실험**: `ticker.cancel()` 을 지우면? / `delay` 대신 `Thread.sleep` 을 쓰면?

### ✅ Step 3. 세션 간 브로드캐스트 — Chat
- `MutableSharedFlow` 로 1:N 팬아웃, `replay` 로 최근 기록 전달
- `BufferOverflow` 전략과 **느린 소비자(slow consumer)** 문제
- **실험**: `onBufferOverflow` 를 `SUSPEND` 로 바꾸고 느린 클라이언트를 흉내내기

### ⬜ Step 4. 메시지 프로토콜 정의
- `kotlinx.serialization` + `ContentNegotiation` 으로 JSON 메시지 (`sendSerialized` / `receiveDeserialized`)
- sealed class 로 메시지 타입(join/chat/whisper/error) 모델링

### ⬜ Step 5. 다중 채팅방 & 상태 관리
- 방 목록을 `ConcurrentHashMap` vs `Mutex` vs **Actor 패턴(Channel)** 으로 각각 구현해 비교
- 마지막 사용자가 나가면 방 정리하기

### ⬜ Step 6. 견고함
- 인증(쿼리 토큰 / 세션) 후 웹소켓 연결 허용
- Ping/Pong 타임아웃, 재접속 처리, `withTimeout`
- `SupervisorJob` 과 `CoroutineExceptionHandler` — 한 세션의 예외가 다른 세션에 미치는 영향

### ⬜ Step 7. 부하 & 관찰
- Ktor 클라이언트로 동시 1,000+ 연결 부하 테스트 (코루틴 vs 스레드 비용 체감)
- `Dispatchers.IO` / `Default` / `limitedParallelism` 차이 측정
- 코루틴 디버깅: `-Dkotlinx.coroutines.debug` (로그의 `@coroutine#N` 이 여기서 나옴)

## 참고
- [Ktor WebSockets](https://ktor.io/docs/server-websockets.html)
- [Kotlin Coroutines guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [SharedFlow / StateFlow](https://kotlinlang.org/docs/flow.html)
