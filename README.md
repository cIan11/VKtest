# Pipeline Graph Service

Небольшой REST-сервис для хранения графов пайплайнов. Пайплайн состоит из узлов и направленных зависимостей между ними. Зависимость `A -> B` означает, что узел `A` должен идти раньше узла `B`.

## Технологии

- Java 21
- Spring Boot
- Spring WebMVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway для миграций БД
- Docker Compose для запуска приложения и базы
- JUnit 5 / Mockito для unit-тестов
- Swagger UI для ручной проверки API

## Запуск

```bash
docker compose up --build
```

Приложение будет доступно на `http://localhost:8080`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

PostgreSQL проброшен наружу на порт `5433`, чтобы не конфликтовать с локальной БД. Внутри Docker приложение подключается к `postgres:5432`.

## API

| Метод | URL | Описание |
| --- | --- | --- |
| `POST` | `/pipelines` | Создать пайплайн |
| `POST` | `/pipelines/{pipelineId}/nodes` | Добавить узел в пайплайн |
| `POST` | `/pipelines/{pipelineId}/edges` | Добавить зависимость между узлами |
| `GET` | `/pipelines/{pipelineId}` | Получить пайплайн целиком |
| `GET` | `/pipelines/{pipelineId}/execution-order` | Получить корректный порядок выполнения узлов |

Тела запросов:

`POST /pipelines`

```json
{
  "name": "main"
}
```

`POST /pipelines/{pipelineId}/nodes`

```json
{
  "name": "input"
}
```

`POST /pipelines/{pipelineId}/edges`

```json
{
  "sourceNodeId": "uuid",
  "targetNodeId": "uuid"
}
```

## Решения

Схема БД создается через Flyway. Это нужно, чтобы при запуске через Docker структура таблиц создавалась одинаково и предсказуемо. Hibernate настроен в режиме `validate`: он не создает таблицы сам, а только проверяет соответствие entity и схемы.

Swagger UI добавлен для удобной ручной проверки API без Postman и длинных curl-команд.

Docker Compose поднимает сразу приложение и PostgreSQL. Healthcheck у базы нужен, чтобы приложение запускалось после готовности PostgreSQL.

GlobalExceptionHandler используется для единых ошибок API: например, `404` для отсутствующих сущностей, `400` для некорректного запроса и `409` для конфликтов вроде цикла или дубля зависимости.

Циклы проверяются при добавлении зависимости. Если новое ребро приводит к циклу, сервис возвращает ошибку `409 Conflict`.

Порядок выполнения считается топологической сортировкой: сначала выбираются узлы без входящих зависимостей, затем постепенно добавляются следующие доступные узлы.

## Тесты

```bash
./gradlew test
```
