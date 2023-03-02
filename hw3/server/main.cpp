#include "server.h"

int main(int argc, char* argv[]) {
    if (argc < 3) {
        fprintf(stderr, "Usage: main <port number> <thread number>\n");
        return -1;
    }
    setup_sigterm_handler();

    int port = atoi(argv[1]);
    int threads = atoi(argv[2]);

    try {
        FileStorage storage; 
        Server server(port, 4096, threads, &storage);
        server.start(); 
    } catch (std::runtime_error& e) {
        std::cerr << e.what() << std::endl;
    }

    return 0;
}
