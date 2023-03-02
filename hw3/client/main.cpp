#include <stdio.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <iostream>
#include <unistd.h>
#include <string>
#include <cstring>
#include <cassert>

void write_to_server(int conn, const std::string& res) {
    while (send(conn, res.c_str(), res.length(), 0) == -1) {}
}   

void send_http_query(int conn, std::string file_name) {
    std::string query = "GET /" + file_name + " HTTP/1.1";
    write_to_server(conn, query);
}


std::string read_response(int conn, char* buf, int buffer_size) {
    while (true) {
        int bytes_n = recv(conn, buf, buffer_size - 1, 0);
        if (bytes_n == 0 || bytes_n == -1) {
            throw std::runtime_error("failed to read response");
        }
        buf[bytes_n] = '\0';
        break;
    }

    std::string resp(buf);
    size_t start = resp.find("\n\n");
    if (start == std::string::npos) {
        throw std::runtime_error("file does not exist");
    }
    start += 2;
    return resp.substr(start, resp.size() - start);
}



int main(int argc, char* argv[]) {
    if (argc < 4) {
        std::cout << "Usage: main <addr> <port> <file_name>" << std::endl;
        return -1;
    }

    std::string addr(argv[1]);
    int port = atoi(argv[2]);
    std::string file_name(argv[3]);

    const uint32_t BUFFER_LEN = 4096;
    char buf[BUFFER_LEN];

    int tcp_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (tcp_socket == -1)
    {
        std::cout << "socket creation error" << std::endl;
        return 0;
    }

    sockaddr_in addr_remote;
    socklen_t addr_remote_len = sizeof(addr_remote);
    memset(reinterpret_cast<uint8_t*>(&addr_remote), 0, addr_remote_len);
    addr_remote.sin_family = AF_INET;
    addr_remote.sin_addr.s_addr = inet_addr("127.0.0.1");
    addr_remote.sin_port = htons(port);

    int connect_res = connect(tcp_socket, reinterpret_cast<sockaddr*>(&addr_remote), addr_remote_len);
    if (connect_res == -1) {
        std::cout << "Socket connect error" << std::endl;
        close(tcp_socket);
        return 0;
    }

    send_http_query(tcp_socket, file_name);
    try {
        std::cout << read_response(tcp_socket, buf, BUFFER_LEN) << std::endl;
    } catch (const std::runtime_error& e) {
        std::cout << "FAIL: " << e.what() << std::endl;
    }

    close(tcp_socket);
    return 0;
}