import socket
import argparse


def find_available_ports(host, port_begin, port_end):
    res = []
    for p in range(port_begin, port_end):
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.bind((host, p))
            res.append(p)
        except Exception:
            continue
    return res


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='port tool')
    parser.add_argument("--host", type=str, help='server host')
    parser.add_argument("--port_begin", type=int, help='begin of port range (inclusively)')
    parser.add_argument("--port_end", type=int, help='end of port range (exclusively)')
    args = parser.parse_args()

    res = find_available_ports(args.host, args.port_begin, args.port_end)
    if len(res) == 0:
        print("no available ports")
    else:
        print(f'available ports: {" ".join(list(map(str, res)))}')
