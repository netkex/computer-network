import argparse
import subprocess
import socket


def rpc_server(host, port):
    server = socket.socket()
    server.bind((host, port))

    server.listen(1)
    while True:
        conn, addr = server.accept()
        print(f"NEW CLIENT: {str(addr)}")

        rpc = conn.recv(2048)
        if not rpc:
            print(f"failed to get rpc from {str(addr)}")
            continue

        rpc = str(rpc, 'utf-8')
        print(f"RPC: {rpc}")

        res = subprocess.run(rpc.split(' '), stdout=subprocess.PIPE)
        conn.send(res.stdout)

        conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='rpc server')
    parser.add_argument("--host", type=str, help='server host')
    parser.add_argument("--port", type=int, help='server port')
    args = parser.parse_args()

    rpc_server(args.host, args.port)
