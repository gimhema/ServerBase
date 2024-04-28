module Go

go 1.16

// 서버 모듈
replace Go/server => ./server

// 클라이언트 모듈
replace Go/client => ./client

require (
    // 필요한 의존성이 있다면 여기에 추가합니다
)