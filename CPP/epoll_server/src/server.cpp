#include <iostream>
#include <cstring>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/epoll.h>
#include <vector>

const int MAX_EVENTS = 10;
const int PORT = 8080;

int main() {
    int server_fd, client_fd;
    struct sockaddr_in server_addr, client_addr;
    socklen_t client_len = sizeof(client_addr);

    // Create socket
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("socket failed");
        return 1;
    }

    // Set server address
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(PORT);

    // Bind socket
    if (bind(server_fd, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0) {
        perror("bind failed");
        return 1;
    }

    // Listen
    if (listen(server_fd, 3) < 0) {
        perror("listen failed");
        return 1;
    }

    // Create epoll instance
    int epoll_fd = epoll_create1(0);
    if (epoll_fd == -1) {
        perror("epoll_create1 failed");
        return 1;
    }

    struct epoll_event event;
    event.events = EPOLLIN;
    event.data.fd = server_fd;

    // Add server socket to epoll
    if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, server_fd, &event) == -1) {
        perror("epoll_ctl: server_fd");
        return 1;
    }

    std::vector<struct epoll_event> events(MAX_EVENTS);

    while (true) {
        int num_events = epoll_wait(epoll_fd, events.data(), MAX_EVENTS, -1);
        if (num_events == -1) {
            perror("epoll_wait");
            return 1;
        }

        for (int i = 0; i < num_events; ++i) {
            if (events[i].data.fd == server_fd) {
                // Accept new connection
                if ((client_fd = accept(server_fd, (struct sockaddr *)&client_addr, &client_len)) < 0) {
                    perror("accept failed");
                    return 1;
                }

                // Add client socket to epoll
                event.events = EPOLLIN | EPOLLET; // Edge-triggered mode
                event.data.fd = client_fd;
                if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client_fd, &event) == -1) {
                    perror("epoll_ctl: client_fd");
                    return 1;
                }

                std::cout << "New connection from " << inet_ntoa(client_addr.sin_addr) << std::endl;
            } else {
                // Handle data from client
                char buffer[1024];
                ssize_t bytes_received = recv(events[i].data.fd, buffer, sizeof(buffer), 0);
                if (bytes_received <= 0) {
                    // Connection closed or error
                    if (bytes_received == 0) {
                        std::cout << "Connection closed by client" << std::endl;
                    } else {
                        perror("recv");
                    }
                    close(events[i].data.fd);
                    continue;
                }

                // Echo received data back to client
                send(events[i].data.fd, buffer, bytes_received, 0);
            }
        }
    }

    close(server_fd);
    return 0;
}