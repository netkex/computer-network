# HW 5, Павленко Даниил

## Программирование сокетов. 


Для всех программ можно запустить с флагом `-h`, в таком случае будут выведены все флаги с их описанием: 
```bash 
$ python3 mail/mail_framework.py -h
usage: mail_framework.py [-h] [-s SNDR] [-p PSWRD] [-r RCVR] [-f FILE] [-su SUBJECT] [-fr FROMADR]

mail sender

options:
  -h, --help            show this help message and exit
  -s SNDR, --sndr SNDR  mail address from which mail will be sent
  -p PSWRD, --pswrd PSWRD
                        mail client password
  -r RCVR, --rcvr RCVR  mail address to which mail will be sent
  -f FILE, --file FILE  content to send
  -su SUBJECT, --subject SUBJECT
                        mail message subject
  -fr FROMADR, --fromadr FROMADR
                        from address in mail body
```

### Почта и SMTP

Задание выполнено в директории [mail](mail/). 

Задание A выполнено в фaйле [mail_framework.py](mail/mail_framework.py). Пример запуска программыы: 
```bash 
python3 mail_framework.py -s <yout_mail@email.com> -p <mail_password> -r <receiver_email@email.com> -f example.html -su test
```

Задания Б и В выполнено в фaйле [mail_socket.py](mail/mail_socket.py). Пример запуска программы: 
```bash 
python3 mail_socket.py -s <yout_mail@email.com> -p <mail_password> -r <receiver_email@email.com> -f example.jpeg -su test
```

В обоих случаях контент письма определяется по расширению передаваемого файла: в случае `.txt` отправляется обычное текстовое письмо, 
`.html` отправляется в формате `html`, а в случае `.jpeg` и `.png` отправляется в письмо с прикрепленным изображением. 

### Удаленный запуск команд

Задание выполнено в директории [rpc](rpc/). 

Код сервера лежит в файле [server.py](rpc/server.py), он принимает по одному запросы на выполнение `bash` команд, выполняет локально 
и возвращает результат клиенту. Пример запуска сервера с частичным выводом: 
```bash 
$ python3 server.py --host localhost --port 3000

NEW CLIENT: ('127.0.0.1', 49230)
RPC: ls -la
```

Код клиента лежит в файле [client.py](rpc/client.py), он принимает `host` и `port` сервера, а также баш запрос, отправляет этот запрос на сервер и пишет результат в консоль. 
Пример запуска клиента с выводом: 

```bash 
$ python3 client.py --host localhost --port 3000 --rpc "ls -la"

Connecting to server...
Sending query
Response: total 16
drwxr-xr-x  4 netkex  staff  128 Mar 29 00:02 .
drwxr-xr-x  7 netkex  staff  224 Mar 30 00:33 ..
-rw-r--r--  1 netkex  staff  693 Mar 29 00:02 client.py
-rw-r--r--  1 netkex  staff  856 Mar 29 00:02 server.py
```

### Широковещательная рассылка через UDP

Задание выполнено в директории [broadcast](broadcast/). 

Код сервера лежит в файле [server.py](broadcast/server.py), он раз в секунду отправляет `broadcast` udp со своим временем. 
Пример запуска сервера с частичным выводом: 
```bash 
$ python3 server.py -p 5000 -sp 3000

time 00:41:03 sent!
time 00:41:04 sent!
time 00:41:05 sent!
```

Код клиента лежит в файле [client.py](broadcast/client.py), он принимает все UDP сообщения и выводит их в консоль. 
Пример запуска сервера с частичным выводом: 
```bash 
$ python3 client.py -p 5000

received time: 00:41:03
received time: 00:41:04
received time: 00:41:05
```