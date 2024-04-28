cmake_minimum_required(VERSION 3.0)
project(epoll_tcp_server)

set(CMAKE_CXX_STANDARD 11)

add_executable(epoll_tcp_server src/server.cpp)

# Find required libraries
find_package(Threads REQUIRED)

# Link required libraries
target_link_libraries(epoll_tcp_server Threads::Threads)