import argparse
import socket


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='broadcast receiver')
    parser.add_argument("-p", "--port", type=int, help='receiver port')
    args = parser.parse_args()

    client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)

    client.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    client.bind(("", args.port))
    while True:
        time, addr = client.recvfrom(2048)
        print(f"received time: {str(time, 'utf-8')}")
