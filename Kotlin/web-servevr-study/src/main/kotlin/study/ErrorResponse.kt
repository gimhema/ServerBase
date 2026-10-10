package study

import kotlinx.serialization.Serializable

// 모든 HTTP API의 실패 응답 형식
// code: 클라이언트가 분기할 때 쓰는 고정 문자열, message: 사람이 읽는 설명
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String
)
