#include <iostream>
#include <cstring>
#include <arpa/inet.h>
#include <unistd.h>
#include <vector>

// 데이터 구조 정의
struct Data {
    uint32_t id;
    double value;
    uint32_t name_len;
    std::vector<char> name;
};

// 데이터 직렬화
std::vector<char> serialize(const Data& data) {
    std::vector<char> buffer;
    buffer.resize(sizeof(data.id) + sizeof(data.value) + sizeof(data.name_len) + data.name.size());

    size_t offset = 0;
    std::memcpy(&buffer[offset], &data.id, sizeof(data.id));
    offset += sizeof(data.id);

    std::memcpy(&buffer[offset], &data.value, sizeof(data.value));
    offset += sizeof(data.value);

    std::memcpy(&buffer[offset], &data.name_len, sizeof(data.name_len));
    offset += sizeof(data.name_len);

    std::memcpy(&buffer[offset], data.name.data(), data.name.size());

    return buffer;
}

// 데이터 역직렬화
Data deserialize(const std::vector<char>& buffer) {
    Data data;
    size_t offset = 0;

    std::memcpy(&data.id, &buffer[offset], sizeof(data.id));
    offset += sizeof(data.id);

    std::memcpy(&data.value, &buffer[offset], sizeof(data.value));
    offset += sizeof(data.value);

    std::memcpy(&data.name_len, &buffer[offset], sizeof(data.name_len));
    offset += sizeof(data.name_len);

    data.name.resize(data.name_len);
    std::memcpy(data.name.data(), &buffer[offset], data.name_len);

    return data;
}

int main() {
    // 서버에 연결
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        std::cerr << "Failed to create socket." << std::endl;
        return 1;
    }

    sockaddr_in server_addr{};
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(7878);
    inet_pton(AF_INET, "127.0.0.1", &server_addr.sin_addr);

    if (connect(sock, (sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        std::cerr << "Failed to connect to server." << std::endl;
        return 1;
    }

    // 데이터 생성 및 직렬화
    Data data = {42, 3.14159, 12, std::vector<char>{'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!'}};
    std::vector<char> serialized = serialize(data);

    // 데이터 전송
    send(sock, serialized.data(), serialized.size(), 0);

    // 서버로부터 데이터 받기
    char buffer[1024];
    int bytes_received = recv(sock, buffer, 1024, 0);
    
    std::vector<char> recv_data(buffer, buffer + bytes_received);
    Data received_data = deserialize(recv_data);

    // 받은 데이터 출력
    std::cout << "Received from server: id=" << received_data.id
              << ", value=" << received_data.value
              << ", name=" << std::string(received_data.name.begin(), received_data.name.end()) << std::endl;

    close(sock);
    return 0;
}
