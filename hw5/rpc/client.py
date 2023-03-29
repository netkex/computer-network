import argparse
import socket


def rpc_client(host, port, rpc):
    client = socket.socket()

    print("Connecting to server...")
    client.connect((host, port))

    print("Sending query")
    client.send(rpc.encode('utf-8'))

    response = str(client.recv(2048), 'utf-8')
    print(f"Response: {response}")

    client.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='rpc client')
    parser.add_argument("--host", type=str, help='server host')
    parser.add_argument("--port", type=int, help='server port')
    parser.add_argument("--rpc", type=str, help='rpc query')
    args = parser.parse_args()

    rpc_client(args.host, args.port, args.rpc)
