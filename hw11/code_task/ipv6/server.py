import socket
import argparse


def server(host: str, port: int):
    server_socket = socket.socket(socket.AF_INET6, socket.SOCK_STREAM, 0)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 0)
    server_socket.bind((host, port))
    server_socket.listen(1)
    return server_socket


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='port tool')
    parser.add_argument("--host", type=str, help='server host')
    parser.add_argument("--port", type=int, help='server port')
    args = parser.parse_args()

    with server(args.host, args.port) as s:
        while True:
            conn, addr = s.accept()
            msg = conn.recv(1024).decode("utf-8")

            print(f"Incoming message: {msg}")
            reply = f"Echo: {msg}"
            conn.send(bytes(reply, 'utf-8'))
            conn.close()
