# HW 11, Павленко Даниил

### Программирование. 

Код с выполненными заданиями находятся в директории [code_task](code_task).

#### Дистанционно-векторная маршрутизация

Для демонстрации программа интерактивная: в процессе работы можно изменять параметры ребер и будет происходить пересчет
всех расстояний. При этом начальное состояние графа, количество вершин в графе и степень параллелизма задается в 
[конфигe](code_task/distributed_dijkstra/src/main/resources/config.json).

Лог ведется параллельно в консоль и в файл, пример лога: [dijkstra.log](code_task/distributed_dijkstra/dijkstra.log).

Для сборки и запуска программы достаточно выполнить следующую команду из корня директории [distributed_dijkstra](code_task/distributed_dijkstra)
```bash
./gradlew run --args="-c <path to config>"  
```

Пример запуска:
```bash 
./gradlew run --args="-c src/main/resources/config.json"  
```

#### IPv6

##### Сервер

Для сборки и запуска сервера достаточно выполнить следующую команду из корня директории [ipv6](code_task/ipv6):
```bash 
python3 server.py --host <host> --port <port>
```

Пример запуска:
```bash 
python3 server.py --host localhost --port 9000
```

Пример лога сервера: 
```
Incoming message: Hello IPv6
```

##### Клиент

Для сборки и запуска клиента достаточно выполнить следующую команду из корня директории [ipv6](code_task/ipv6):
```bash 
python3 client.py --host <server host> --port <server port> --data <message>
```

Пример запуска:
```bash 
python3 client.py --host localhost --port 9000 --data "Hello IPv6"
```

Пример лога клиента: 
``` 
Echo: Hello IPv6
```