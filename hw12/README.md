# HW 12, Павленко Даниил

### Программирование.

Код с выполненными заданиями находятся в директории [code_task](code_task).

#### RIP

Для демонстрации программа интерактивная: в процессе работы можно останавливать работу маршрутизаторов и будет происходить пересчет
всех расстояний. При этом начальное состояние сети, количество роутеров в сети и степень параллелизма задается в
[конфигe](code_task/rip/src/main/resources/config.json).

Лог ведется параллельно в консоль и в файл, пример лога: [rip.log](code_task/rip/rip.log).

Для сборки и запуска программы достаточно выполнить следующую команду из корня директории [rip](code_task/rip)
```bash
./gradlew run --args="-c <path to config>"  
```

Пример запуска:
```bash 
./gradlew run --args="-c src/main/resources/config.json"  
```

Пример лога:
```bash 
[pool-1-thread-1] INFO  Router  - [Router 198.71.43.61] Update from neighbour: 157.105.66.180
[pool-1-thread-2] INFO  Router  - [Router 199.239.33.66] Update from neighbour: 42.162.54.248
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 42.162.54.248
[pool-1-thread-4] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 42.162.54.248
[pool-1-thread-5] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 199.239.33.66
[pool-1-thread-4] INFO  Router  - [Router 122.136.243.149] Path to 157.105.66.180. Next hop: 42.162.54.248. Number of hops: 2
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Path to 199.239.33.66. Next hop: 42.162.54.248. Number of hops: 2
[pool-1-thread-2] INFO  Router  - [Router 199.239.33.66] Path to 157.105.66.180. Next hop: 42.162.54.248. Number of hops: 2
[pool-1-thread-1] INFO  Router  - [Router 198.71.43.61] Path to 42.162.54.248. Next hop: 157.105.66.180. Number of hops: 2
[pool-1-thread-5] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 122.136.243.149
[pool-1-thread-2] INFO  Router  - [Router 199.239.33.66] Path to 122.136.243.149. Next hop: 42.162.54.248. Number of hops: 2
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Path to 122.136.243.149. Next hop: 42.162.54.248. Number of hops: 2
[pool-1-thread-4] INFO  Router  - [Router 122.136.243.149] Path to 199.239.33.66. Next hop: 42.162.54.248. Number of hops: 2
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 198.71.43.61
[pool-1-thread-5] INFO  Router  - [Router 42.162.54.248] Path to 198.71.43.61. Next hop: 122.136.243.149. Number of hops: 2
[pool-1-thread-1] INFO  Router  - [Router 198.71.43.61] Update from neighbour: 122.136.243.149
[pool-1-thread-5] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 157.105.66.180
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Path to 122.136.243.149. Next hop: 198.71.43.61. Number of hops: 2
[pool-1-thread-4] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 198.71.43.61
[pool-1-thread-1] INFO  Router  - [Router 198.71.43.61] Path to 42.162.54.248. Next hop: 122.136.243.149. Number of hops: 2
[pool-1-thread-4] INFO  Router  - [Router 122.136.243.149] Path to 157.105.66.180. Next hop: 198.71.43.61. Number of hops: 2
...
[main] INFO  Router  - [Router 198.71.43.61] Was stopped by user
[pool-1-thread-5] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 198.71.43.61
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 199.239.33.66
[pool-1-thread-2] INFO  Router  - [Router 199.239.33.66] Update from neighbour: 42.162.54.248
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 42.162.54.248
[pool-1-thread-2] INFO  Router  - [Router 199.239.33.66] Path to 198.71.43.61. Next hop: 42.162.54.248. Number of hops: 3
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 122.136.243.149
[pool-1-thread-5] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 42.162.54.248
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 157.105.66.180
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 198.71.43.61
[pool-1-thread-2] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 42.162.54.248
[pool-1-thread-1] INFO  Router  - [Router 199.239.33.66] Update from neighbour: 42.162.54.248
...
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Path to 198.71.43.61. Next hop: 157.105.66.180. Number of hops: 12
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 199.239.33.66
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 157.105.66.180
[pool-1-thread-1] INFO  Router  - [Router 42.162.54.248] Path to 198.71.43.61. Next hop: 122.136.243.149. Number of hops: 14
[pool-1-thread-4] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 122.136.243.149
[pool-1-thread-5] INFO  Router  - [Router 199.239.33.66] Update from neighbour: 42.162.54.248
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 42.162.54.248
[pool-1-thread-2] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 42.162.54.248
[pool-1-thread-3] INFO  Router  - [Router 157.105.66.180] Path to 198.71.43.61. Next hop: 42.162.54.248. Number of hops: 15
[pool-1-thread-5] INFO  Router  - [Router 199.239.33.66] Path to 198.71.43.61. Next hop: 42.162.54.248. Number of hops: 15
[pool-1-thread-4] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 199.239.33.66
[pool-1-thread-2] INFO  Router  - [Router 122.136.243.149] Path to 198.71.43.61. Next hop: 42.162.54.248. Number of hops: 15
[pool-1-thread-4] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 157.105.66.180
[pool-1-thread-3] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 199.239.33.66
[pool-1-thread-2] INFO  Router  - [Router 199.239.33.66] Update from neighbour: 42.162.54.248
[pool-1-thread-5] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 42.162.54.248
[pool-1-thread-1] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 42.162.54.248
[pool-1-thread-3] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 157.105.66.180
[pool-1-thread-3] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 122.136.243.149
[pool-1-thread-2] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 122.136.243.149
[pool-1-thread-2] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 157.105.66.180
[pool-1-thread-1] INFO  Router  - [Router 199.239.33.66] Update from neighbour: 42.162.54.248
[pool-1-thread-5] INFO  Router  - [Router 122.136.243.149] Update from neighbour: 42.162.54.248
[pool-1-thread-4] INFO  Router  - [Router 157.105.66.180] Update from neighbour: 42.162.54.248
[pool-1-thread-2] INFO  Router  - [Router 42.162.54.248] Update from neighbour: 199.239.33.66
```