# HW 8, Павленко Даниил

### Реализация протокола Stop and Wait 

Код с выполненным заданием находится в директории [code_task](code_task). 

Для сборки и запуска сервера достаточно выполнить следующую команду из корня директории.

```bash 
./gradlew server:run --args="-p <server port> -t <[optional] timeout in ms> -pl <[optional] lost packet probability>"
```

Пример запуска: 
```bash 
./gradlew server:run --args="-p 6000"
```

В таком случае сервер будет ждать подключения клиента, после чего примет файл и сохранит его 
в [received.txt](code_task/server/src/main/resources/received.txt).

Пример лога работы программы: 
```bash 
Message was corrupted! Original check sum: 543516756; data check sum: 1526534339
Current total size of received data: 4096
Current total size of received data: 8192
Message was corrupted! Original check sum: 543518049; data check sum: 3975684839
Message was corrupted! Original check sum: 543518049; data check sum: 3975684839
Current total size of received data: 12288
Current total size of received data: 16384
Current total size of received data: 20480
Current total size of received data: 24576
Message was corrupted! Original check sum: 1819244137; data check sum: 581418807
Current total size of received data: 28672
Current total size of received data: 32768
Current total size of received data: 36864
Message was corrupted! Original check sum: 1818458467; data check sum: 2150226655
Current total size of received data: 40960
Message was corrupted! Original check sum: 1663060256; data check sum: 4143644082
Current total size of received data: 45056
Current total size of received data: 49152
Message was corrupted! Original check sum: 665381943; data check sum: 1183050858
Message was corrupted! Original check sum: 543516788; data check sum: 1842197571
Current total size of received data: 53248
Message was corrupted! Original check sum: 1969448307; data check sum: 524818002
Message was corrupted! Original check sum: 1781232417; data check sum: 1781232458
Current total size of received data: 53434
content was successfully received
```

Для сборки и запуска клиента достаточно выполнить следующую команду из корня директории.

```bash
./gradlew server:run --args=" -p <server port> -fl <file to transfer> --host <[optional] server host> -t <[optional] timeout in ms> -pl <[optional] lost packet probability>"
```

Пример запуска: 
```bash 
./gradlew client:run --args="-p 6000 -fl src/main/resources/holmes.txt"
```
 
Тесты для чек сумм имплементированны прям в клиента: в случае отправки пакета, с вероятностью `pl` будут выбраны и заменены случайны 3 байта данных пакета. 

Пример лога работы программы:
```bash 
new byte iterator value: 4096
new byte iterator value: 8192
new byte iterator value: 12288
new byte iterator value: 16384
new byte iterator value: 20480
new byte iterator value: 24576
new byte iterator value: 28672
new byte iterator value: 32768
new byte iterator value: 36864
new byte iterator value: 40960
new byte iterator value: 45056
new byte iterator value: 49152
new byte iterator value: 53248
new byte iterator value: 57344
content was successfully sent
```

Для теста использовался текст о Шерлоке Холмсе на английском. Для проверки корректности можно запустить `diff`:

```bash 
diff client/src/main/resources/holmes.txt server/src/main/resources/received.txt
```

### Задачи 

#### Задача 1 

Заметим, что при долгой передаче данных для расчета средней пропускной способности TCP можно пренебречь фазой "медленного старта", 
а тогда останутся только последовательный фазы, когда TCP-окно увеличивается линейно от $\frac{W}{2}$ до $W$. 
Но тогда средняя пропускная способность TCP соответствует средней пропускной способности во время увеличения окна от $\frac{W}{2}$ до $W$, а это 
функция от количества $RTT$ за эту фазу, то есть T. 

Что и требовалось. 

### Задача 3 

Пусть $T$ - такое минимальное количество полученных ACK-ов, что начиная с размера окна $\frac{W}{2}$ мы дойдем до размера окна $\ge W$. Тогда 
утверждается, что частота потерь $L = \frac{1}{T}$ (если мы можем пренебречь фазой медленного старта). Но заметим, что 
$T = \lceil \log_{1 + \alpha}(\frac{W}{\frac{W}{2}}) \rceil = \lceil \log_{1 + \alpha} (2) \rceil$, что константа. То есть, 
$L = \frac{1}{\lceil \log_{1 + \alpha} (2) \rceil}$. 

Если же рассматривать случай, когда после таймаута размер окна падает до 1, то $T = \lceil \log_{1 + \alpha} (W) \rceil$, а 
$L = \frac{1}{\lceil \log_{1 + \alpha} (W) \rceil}$

