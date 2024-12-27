## List of services od their dependencies

- `db-delivery`
- `srv-delivery`


## Cheatsheet

#### Connect to PostgreSQL and enter the password:
```shell
docker exec -it db-delivery psql -d db_delivery -U db-user
```

#### In PostgreSQL CLI, set default schema in the current session to `delivery`:
```shell
SET search_path = delivery;
```
