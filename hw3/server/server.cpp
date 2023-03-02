#include "server.h"

bool exit_ = false;
void sigint_handler_fun(int) {
    exit_ = true;
}

Server::Server(int port_, int buffer_size_, int thread_num_, FileStorage* storage_)
    :port(port_),  buffer_size(buffer_size_), thread_num(thread_num_), storage(storage_) {
    for (int i = 0; i < thread_num; i++) {
        threads.push_back(std::thread(&Server::handle_event, this));
    }
}

Server::~Server() {
    for (auto &t : threads) {
        if (t.joinable())
            t.join();
    }   
    for (auto conn: active_conn) {
        close(conn);
    }
    close(tcp_socket);
}

void Server::start() {
    open_tcp();

    int lister_result = listen(tcp_socket, 10000);
    if (lister_result == -1)
        throw std::runtime_error("failed setup listener on tcp socket");

    epoll_fd = epoll_create(1);
    if (epoll_fd < 0 || !set_nonblocking(tcp_socket) || !start_conn_epoll(tcp_socket))
        throw std::runtime_error("failed to setup epoll");

    sockaddr_in addr_remote;
    socklen_t addr_remote_len = sizeof(addr_remote);
    memset(&addr_remote, 0, addr_remote_len);
    
    epoll_event events[MAX_EVENTS];
    
    std::cerr << "start server " << port << std::endl;
    while (true) {
        if (exit_) {
            break;
        }
        
        int wait_res = epoll_wait(epoll_fd, events, MAX_EVENTS, -1);
        if (wait_res == -1) {
            std::cerr << "incrorect epoll result" << std::endl;
            continue;
        }

        std::vector<int> read_conns;
        for (int i = 0; i < wait_res; i++) {
            int cur_fd = events[i].data.fd;
            uint32_t cur_events = events[i].events;
            if (cur_events & EPOLLERR) {
                _close(cur_fd);
                continue;
            } 
            if (cur_fd == tcp_socket) {
                _accept(tcp_socket, &addr_remote, addr_remote_len);
                continue;
            }
            if (cur_events & EPOLLIN)
            {
                _remove(cur_fd);
                read_conns.push_back(cur_fd);
                continue;
            }
        }

        event_queue_lock.lock();
        for (auto conn : read_conns) {
            event_queue.push_back({conn});
        }
        event_queue_lock.unlock();
    }
}

void Server::open_tcp() {
    tcp_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (tcp_socket == -1) {
        throw std::runtime_error("failed to open socket");
    }
    
    sockaddr_in addr_local;
    memset(&addr_local, 0, sizeof(addr_local));
    addr_local.sin_family = AF_INET;
    addr_local.sin_port = htons(port);
    addr_local.sin_addr.s_addr = htonl(INADDR_ANY);
    int bind_result = bind(
        tcp_socket, 
        reinterpret_cast<sockaddr*>(&addr_local),
        sizeof(addr_local)
    );

    if(bind_result == -1) {   
        close(tcp_socket);
        throw std::runtime_error("failed to bind socket");
    }
} 

bool Server::set_nonblocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags == -1) {
        return false;
    }

    int res = fcntl(fd, F_SETFL, flags | O_NONBLOCK);
    if (res != 0) {
        return false;
    }

    return true;
}

bool Server::start_conn_epoll(int conn) {
    epoll_event event{};
    event.data.fd = conn;
    event.events = EPOLLIN | EPOLLET;

    int res = epoll_ctl(epoll_fd, EPOLL_CTL_ADD, conn, &event);
    return (res != -1);
}

void Server::_accept(int tcp_socket, sockaddr_in* addr_remote_ptr, socklen_t addr_remote_len) {
    while (true) {
        int conn = accept(tcp_socket, reinterpret_cast<sockaddr*>(addr_remote_ptr), &addr_remote_len);
        if (conn == -1)
            return;

        if (!set_nonblocking(conn) || !start_conn_epoll(conn)) {
            close(conn);
        } else {
            active_conn.insert(conn);
            std::cerr << "Connected client " << inet_ntoa((*addr_remote_ptr).sin_addr) << ":" << ntohs((*addr_remote_ptr).sin_port) << std::endl;
        }
    }
}

void Server::_remove(int conn) {
    epoll_ctl(epoll_fd, EPOLL_CTL_DEL, conn, nullptr);
}

void Server::_close(int conn) {
    _remove(conn);
    close(conn);
    active_conn.erase(conn);
}

void Server::handle_event() {
    char* buf = static_cast<char*>(malloc(buffer_size));
    
    while (true) {
        event_queue_lock.lock();
        if (event_queue.empty()) {
            event_queue_lock.unlock();
            if (exit_)
                break;
            continue;
        }

        auto event = event_queue.front();
        event_queue.pop_front();
        event_queue_lock.unlock();
        handle_client(event.conn, buf);
    }

    free(buf);
}

void Server::handle_client(int conn, char *buf) {
    while (true) {
        int bytes_n = recv(conn, buf, buffer_size - 1, 0);
        if (bytes_n == 0) {
            start_conn_epoll(conn);
            return;
        }
        if (bytes_n == -1) {
            start_conn_epoll(conn);
            return;
        }
    
        buf[bytes_n] = '\0';
        std::string query(buf);
        process_query(conn, query);
    }
}

void Server::process_query(int conn, const std::string& query_str) {
    std::string file_content;
    try {
        const Query& query = parse_string(query_str);
        file_content = storage->get(query.file_name);
    } catch (const std::runtime_error& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        send_server_error(conn);
        return;
    } 

    send_content(conn, file_content);
}

void Server::send_server_error(int conn) { 
    send_http_response(conn, 404, "Not Found");
} 

void Server::send_content(int conn, std::string file_content) { 
    send_http_response(conn, 200, "OK", "text/plain", file_content);
}

void Server::send_http_response(int conn, 
    int status_code, 
    std::string response_phrase, 
    std::string content_type,
    std::string body
) {
    std::string response = "HTTP/1.1 " + std::to_string(status_code) + " " + response_phrase + "\n";
    response += "Connection: Keep-Alive\r\n";
    if (!body.empty()) {
        if (!content_type.empty()) {
            response += "Content-Type: " + content_type + "\n";
        }
        response += "Content-Length: " + std::to_string(body.size()) + "\n\n";
        response += body;
    }
    write_to_client(conn, response);
}

void Server::write_to_client(int conn, const std::string& res) {
    while (send(conn, res.c_str(), res.length(), 0) == -1) {}
}   

Server::Query Server::parse_string(const std::string& query_str) {
    size_t query_begin = query_str.find("GET ");
    if (query_begin == std::string::npos) {
        throw std::runtime_error("incorrect query: " + query_str);
    }
    size_t file_name_begin = query_begin + 5;
    size_t file_name_end = query_str.find(" ", file_name_begin);
    std::string file_name = query_str.substr(file_name_begin, file_name_end - file_name_begin);
    return Query { file_name };
}


void setup_sigterm_handler() {
    exit_ = false;
    struct sigaction sigterm_handler;
    sigterm_handler.sa_handler = sigint_handler_fun;
    sigemptyset(&sigterm_handler.sa_mask);
    sigterm_handler.sa_flags = 0;
    sigaction(SIGTERM, &sigterm_handler, NULL);
}