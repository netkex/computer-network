import argparse
import smtplib
import ssl
from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from pathlib import Path


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

    context = ssl.create_default_context()
    with smtplib.SMTP_SSL("smtp.gmail.com", 465, context=context) as server:
        server.login(sndr_mail, sndr_password)
        server.sendmail(sndr_mail, recvr_mail, message.as_string())


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
