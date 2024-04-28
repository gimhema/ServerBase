package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
)

func main() {
	// 서버 주소
	serverAddr := "127.0.0.1:8080"

	// TCP 서버에 연결
	conn, err := net.Dial("tcp", serverAddr)
	if err != nil {
		fmt.Println("Failed to connect to server:", err)
		return
	}
	defer conn.Close()

	// 표준 입력에서 메시지 입력받기
	scanner := bufio.NewScanner(os.Stdin)
	for {
		fmt.Print("Enter message: ")
		scanner.Scan()
		message := scanner.Text()

		// 서버로 메시지 전송
		_, err := conn.Write([]byte(message))
		if err != nil {
			fmt.Println("Failed to send message to server:", err)
			return
		}

		// 서버로부터 응답 받기
		response := make([]byte, 1024)
		_, err = conn.Read(response)
		if err != nil {
			fmt.Println("Failed to receive response from server:", err)
			return
		}
		fmt.Println("Response from server:", string(response))
	}
}
