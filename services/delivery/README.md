## Сервис "Доставщики"

Сервис доставки заказов. Владеет парком транспортных средств различных категорий,
    с разными техническими характеристиками: скорость доставки, максимальная грузоподъёмность
    и дистанция доставки, стоимость за единицу дистанции. Хранит в себе информацию о сделанных заказах.

- `POST /delivery` creates an order;
- `GET /delivery/ongoing` shows a list of ongoing orders;
- `GET /delivery/stats` shows some aggregate statistics for all order types. 
