import argparse
import base64
import ssl
from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from pathlib import Path
from socket import socket, AF_INET, SOCK_STREAM


def send_mail(sndr_mail, sndr_password, recvr_mail, message_type, content, mail_subject="", sndr_from=None):
    if sndr_from is None:
        sndr_from = sndr_mail

    message = MIMEMultipart("alternative")
    if mail_subject != "":
        message["Subject"] = mail_subject
    message["From"] = sndr_from
    message["To"] = recvr_mail

    if message_type == "text":
        message.attach(MIMEText(content, "plain"))
    if message_type == "html":
        message.attach(MIMEText(content, "html"))
    if message_type == "image":
        message.attach(MIMEImage(content))

    wrapped_socket = socket(AF_INET, SOCK_STREAM)
    context = ssl.create_default_context()
    wrapped_socket = context.wrap_socket(wrapped_socket, server_hostname="smtp.gmail.com")

    print("Connecting...")
    wrapped_socket.connect(("smtp.gmail.com", 465))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Saying hello")
    wrapped_socket.send("HELO Alice\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    crlf_mesg = "\r\n"
    print("Starting AUTH...")
    wrapped_socket.send("AUTH LOGIN\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    user64 = base64.b64encode(sndr_mail.encode('utf-8'))
    pass64 = base64.b64encode(sndr_password.encode('utf-8'))

    print("Sending login")
    wrapped_socket.send(user64)
    wrapped_socket.send(crlf_mesg.encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Sending password")
    wrapped_socket.send(pass64)
    wrapped_socket.send(crlf_mesg.encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Sending sender mail")
    wrapped_socket.send(f"MAIL FROM: <{sndr_from}>\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Sending receiver mail")
    wrapped_socket.send(f"RCPT TO: <{recvr_mail}>\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Sending data header")
    wrapped_socket.send("DATA\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Sending mail body")
    mail_body = message.as_string() + '\r\n'
    wrapped_socket.send(mail_body.encode('utf-8'))
    wrapped_socket.send("\r\n.\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    print("Quitting session")
    wrapped_socket.send("QUIT\r\n".encode('utf-8'))
    response = wrapped_socket.recv(2048)
    print(f"Response:  {str(response, 'utf-8')}")

    wrapped_socket.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='mail sender')
    parser.add_argument("-s", "--sndr", type=str, help='mail address from which mail will be sent')
    parser.add_argument("-p", "--pswrd", type=str, help='mail client password')
    parser.add_argument("-r", "--rcvr", type=str, help='mail address to which mail will be sent')
    parser.add_argument("-f", "--file", type=str, help='content to send')
    parser.add_argument("-su", "--subject", type=str, default="", help="mail message subject")
    parser.add_argument("-fr", "--fromadr", type=str, default="", help='from address in mail body')
    args = parser.parse_args()

    content_type = ""
    if args.file.endswith(".txt"):
        content_type = "text"
    elif args.file.endswith(".html"):
        content_type = "html"
    elif args.file.endswith(".jpg") or args.file.endswith(".png") or args.file.endswith(".jpeg"):
        content_type = "image"
    else:
        print("incorrect file format")
        exit(0)

    content = ""
    try:
        if content_type != "image":
            with open(args.file, "rb") as file:
                content = Path(args.file).read_text()
        else:
            with open(args.file, "rb") as file:
                content = file.read()
    except:
        print(f'failed to read file {args.file}')
        exit(0)

    send_mail(args.sndr, args.pswrd, args.rcvr, content_type, content, args.subject, args.fromadr)
