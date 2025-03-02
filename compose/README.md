## List of services and their dependencies

- `db-warehouse`
- `db-shop`
- `db-delivery`
- `srv-warehouse`
- `srv-delivery`


## Cheatsheet

#### Connect to PostgreSQL and enter the password:

```shell
docker exec -it db-warehouse psql -d db_warehouse -U db-user
```

```shell
docker exec -it db-shop psql -d db_shop -U db-user
```

```shell
docker exec -it db-delivery psql -d db_delivery -U db-user
```

#### In PostgreSQL CLI, set default schema in the current session to `delivery`:
```shell
SET search_path = delivery;
```
