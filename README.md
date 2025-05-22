# 🧠 Proselyte Redis Course

## Описание

Проект демонстрирует различные стратегии кэширования с использованием Redis в микросервисной архитектуре:

* `api` — основной REST API с интеграцией Redis
* `ui` — простой UI-клиент для взаимодействия с API
* `RedisInsight` — визуальный интерфейс для мониторинга Redis
* `PostgreSQL` — используется как основное хранилище данных

Реализованы стратегии кэширования:

* Cache-Aside
* Read-Through
* Write-Through
* Write-Behind
* Full Page Cache

Также присутствует демонстрация проблем при работе с конкурентными запросами и пример использования RedisJSON.

## Архитектура

```
+-------+       REST       +------------+
|  UI   +----------------> |   API      |
+-------+                  +------------+
                              |
               +--------------+--------------+
               |                             |
         Redis Stack                 PostgreSQL
 (RedisJSON, TTL, Insight)         (постоянное ХД)
```

## Эндпоинты

📘 Документация OpenAPI: `api/openapi.yml`
🔗 [OpenAPI Spec](api/openapi.yaml)  
Пример запроса:

```bash
curl -X POST http://localhost:8087/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"name": "Alice", "age": 30}'
```

## Быстрый старт через Docker Compose

```bash
docker-compose up
```

Запускаются:

* API (`localhost:8087`)
* Redis (`localhost:6379`)
* RedisInsight (`localhost:5540`)
* PostgreSQL (`localhost:5433`)
* UI (`localhost:3000`)

## Структура проекта

```
proselyte-redis-course/
├── docker-compose.yml
├── api/
│   ├── build.gradle
│   ├── Dockerfile
│   └── src/... (Spring Boot REST API)
├── ui/
│   ├── Dockerfile
│   └── src/... (Node.js / React UI)
└── README.md
```

## Переменные окружения

`.env` или `application.yml` в модуле `api`:

```yaml
spring:
  redis:
    host: redis
    port: 6379
  datasource:
    url: jdbc:postgresql://postgres:5432/proselyte_redis_course
    username: postgres
    password: postgres
```

## Ветки разработки

* `STEP-1` — initial commit of the project with basic docker-compose.yml file
* `STEP-2` — added redisinsight to the docker-compose.yml
* `STEP-3` — added initial api implementation with Redis integration
* `STEP-4` — implemented Cache-Aside caching strategy
* `STEP-5` — implemented Read-Through caching strategy
* `STEP-6` — implemented Write-Through caching strategy
* `STEP-7` — implemented Write-Behind caching strategy
* `STEP-8` — added UI module and implemented Full page caching strategy
* `STEP-9` — added examples for concurrency issues demo
* `STEP-10` — added RedisJSON from RedisStack implementation

## Технологии

* Java 21
* Spring Boot 3 (Web, Data Redis, Scheduling)
* Redis / Redis Stack / RedisInsight
* PostgreSQL
* Docker / Docker Compose
* Node.js / React (UI)

## Автор

[Eugene Suleimanov](https://github.com/proselytear)
[Software Engineering](https://t.me/esuleimanov)
