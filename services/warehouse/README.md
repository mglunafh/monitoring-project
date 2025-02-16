# Сервис "Склад"

Сервис хранения товаров. Хранит различные товары для дальнейшей продажи.
    Ведет реестр поставщиков, а также договоры с поставщиками на поставку товаров.
    Имеется функциональность резервирования товаров.

- `GET /warehouse/suppliers` Shows list of suppliers
- `GET /warehouse/goods` Shows list of registered goods
- `POST /warehouse/contract` Registers a new supply contract
- `GET /warehouse/contract/{id}` Shows contract info by the given ID
- `POST /warehouse/reserve` For the given shopping cart ID, reserves an item, certain amount
- `GET /warehouse/reserve/{id}` Shows reservation info for the given shopping cart
- `POST /warehouse/reserve/cancel` Cancels the reservation
- `POST /warehouse/reserve/purchase` Marks the reservation as purchased 

Доступные Spring-профили:
- `local`. Предназначен для локальной разработки и запуска вместе с экземпляром PostgreSQL СУБД, развёрнутой в Docker.
- `init`. Предназначен для локальной разработки c дополнительной первоначальной
    инициализацией схемы и тестовых данных в экземпляре PostgreSQL СУБД, развернутой в Docker.
