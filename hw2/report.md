## HW 2, Павленко Даниил

## Задачи

------

Так как Markdown не поддерживает LaTeX, решение задач находится в файле [tasks.pdf](tasks.pdf).

## Rest Service

------

[Код HTTP-сервера](server). 

В качестве примера функциональности сервера рассмотрим последовательность запросов из postman'а. 

* Сначала добавим при помощи `POST` два продукта: `(Sushi, 200)` и `(Pizza, 500)`, где первое поле - название продукта, а второе - цена. 

![POST_product](screenshots/POST_product.png)

* Теперь запросим список всех продуктов и какого-то конкретного продукта по *id* при помощи `GET`

![GET_all](screenshots/GET_all.png)
![GET_product](screenshots/GET_product.png)

* Обновим параметры у пиццы (с *id* = 1), а именно изменим цену с 500 на 450 при помощи PUT запроса. 

![PUT_product](screenshots/PUT_product.png)
![PUT_proof](screenshots/PUT_proof.png)

* Удалим пиццу по *id* при помощи DELETE запроса. 

![DELETE_product](screenshots/DELETE_product.png)
![DELETE_proof](screenshots/DELETE_proof.png)

* Добавим изображение для суши при помощи PUT запроса и проверим, что оно правильно сохранилось при помощи GET запроса. 

![PUT_image](screenshots/PUT_image.png)
![GET_image](screenshots/GET_image.png)



