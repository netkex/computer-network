import argparse
import socket
import time
from datetime import datetime

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='broadcast sender')
    parser.add_argument("-sp", "--server_port", type=int, help="server port")
    parser.add_argument("-p", "--port", type=int, help='receiver port')
    args = parser.parse_args()

    server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)

    # Enable broadcasting mode
    server.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    server.settimeout(1)
    server.bind(('', args.server_port))
    while True:
        cur_time = datetime.now().strftime("%H:%M:%S")
        server.sendto(cur_time.encode('utf-8'), ("<broadcast>", args.port))
        print(f"time {cur_time} sent!")
        time.sleep(1)
