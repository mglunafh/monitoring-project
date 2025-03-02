## IPMN

### Current

- `IPMN-10` -- Monitoring
  - `IPMN-11` -- add metrics to 'Deliveries'
  - `IPMN-17` -- add logging to 'Deliveries'
  - `IPMN-18` -- add tracing to 'Deliveries'
  - `IPMN-21` -- deploy grafana with dashboards (https://stackoverflow.com/questions/63518460/grafana-import-dashboard-as-part-of-docker-compose)
- `IPMN-26` -- think about extracting exceptions into a separate module
- `IPMN-36` -- implement 'Shop' app
  - `IPMN-37` -- add users, basic authentication
  - `IPMN-39` -- database configuration via application properties 

---------

### Done

- `IPMN-38` -- shop: add database support via Exposed
- `IPMN-35` -- Organize dependency version management with libs.versions.toml
- `IPMN-20` -- implement 'Warehouse' app
- `IPMN-34` -- warehouse integration test
- `IPMN-33` -- validation constraints on shopping cart id
- `IPMN-32` -- add 'Get shopping cart info' endpoint
- `IPMN-30` -- Incorporated PSQLException data into database error processing
- `IPMN-31` -- add 'Buy out shopping cart' endpoint
- `IPMN-29` -- add 'Cancel shopping cart' endpoint
- `IPMN-28` -- add 'Reserve a quantity of item' endpoint
- `IPMN-22` -- add JDBC-layer tests
- `IPMN-24` -- add Warehouse to the compose file
- `IPMN-27` -- introduce 'Contract info' endpoint
- `IPMN-23` -- introduce 'Contract' entity, add 'Add contract' endpoint
- `IPMN-25` -- extract DTOs into a common subproject
- `IPMN-21` -- add JDBC-based DAO layer
- `IPMN-2` -- implement 'Deliveries' service
- `IPMN-16` -- BUG '/delivery/stats' somehow always shows 'SENT' status
- `IPMN-19` -- add 'shoppingCartId' to the create-delivery-response
- `IPMN-15` -- create 'docker' Spring profile
- `IPMN-13` -- add docker compose file, configure bootBuildImage
- `IPMN-14` -- create a new Spring `local` profile to run delivery service with in-memory H2
- `IPMN-5` -- add integration tests with H2 / MariaDB / PostgreSQL using Testcontainers
- `IPMN-12` -- fix response model, introduce 'payload' field (kinda blocks integration testing)
- `IPMN-6` -- Customize LocalDateTime serialization format for Spring Boot ObjectMapper
- `IPMN-9` -- implement `stats` endpoint
- `IPMN-8` -- split `FailureType` into two parts
- `IPMN-7` -- implement background order/transport manager
- `IPMN-4` -- add MockMvc tests
- `IPMN-3` -- add repository tests (`@DataJpaTest`)
- `IPMN-1` -- create a structure of the project
