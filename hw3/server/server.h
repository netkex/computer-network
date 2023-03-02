#pragma once
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <cstring>
#include <cassert>
#include <unistd.h>
#include <stdexcept>
#include <thread>
#include <iostream>
#include <vector>
#include <unordered_set>
#include <shared_mutex>
#include <iostream>
#include <fstream>
#include <signal.h>
#include <fcntl.h>
#include <deque>
#include <sys/epoll.h>

#include "storage.h"

class Server { 
public: 
    Server(int port_, int buffer_size_, int thread_num_, FileStorage* storage_);
    ~Server();

    void start();

private: 
    struct Task {
        int conn;
    };

    struct Query {
        std::string file_name;
    };

    void open_tcp();
    bool set_nonblocking(int fd);
    bool start_conn_epoll(int conn);
    void _accept(int tcp_socket, sockaddr_in* addr_remote_ptr, socklen_t addr_remote_len);
    void _remove(int conn);
    void _close(int conn);
    void handle_event();
    void handle_client(int conn, char *buf);
    void process_query(int conn, const std::string& query_str);
    void send_server_error(int conn);
    void send_content(int conn, std::string file_content);
    void send_http_response(int conn, 
        int status_code, 
        std::string response_phrase, 
        std::string content_type = "",
        std::string body = ""
    );
    void write_to_client(int conn, const std::string& res);
    Query parse_string(const std::string& query_str);

    int port;
    int buffer_size;
    int thread_num;
    FileStorage* storage;

    const uint32_t MAX_EVENTS = 1000;

    int epoll_fd;
    int tcp_socket;
    std::vector<std::thread> threads;

    std::deque<Task> event_queue;
    std::shared_mutex event_queue_lock;

    std::unordered_set<int> active_conn;
};

void setup_sigterm_handler();