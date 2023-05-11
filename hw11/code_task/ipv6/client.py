import socket
import argparse


def client():
    client_socket = socket.socket(socket.AF_INET6, socket.SOCK_STREAM, 0)
    client_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    client_socket.setsockopt(socket.IPPROTO_IPV6, socket.IPV6_V6ONLY, 0)
    return client_socket


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='port tool')
    parser.add_argument("--host", type=str, help='server host')
    parser.add_argument("--port", type=int, help='server port')
    parser.add_argument("--data", type=str, help='data to send')
    args = parser.parse_args()

    with client() as c:
        c.connect((args.host, args.port))
        c.send(bytes(args.data, 'utf-8'))
        response = c.recv(1024)
        print(response.decode("utf-8"))
        c.close()