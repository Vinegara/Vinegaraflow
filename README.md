# VinegaraFlow

VinegaraFlow - веб-сервис для управления проектами, задачами, участниками, комментариями, уведомлениями и вложениями. Проект сделан на Java 25 и Spring Boot 4.x по требованиям лабораторной работы.

## Возможности

- регистрация и вход пользователей;
- JWT-аутентификация и авторизация по ролям `ROLE_USER` и `ROLE_ADMIN`;
- CRUD для проектов, задач, ролей, пользователей, участников проекта, комментариев, уведомлений и вложений;
- хранение данных в PostgreSQL через Spring Data JPA;
- миграции базы данных через Liquibase;
- логирование SQL и web-запросов;
- Spring Boot Actuator для health/info/metrics/loggers;
- unit и интеграционные тесты с покрытием JaCoCo выше 70%.

## Стек

- Java 25
- Spring Boot 4.x
- Maven
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Liquibase
- JUnit 5, Mockito, MockMvc
- Docker Compose для локальной базы данных

## Сущности

- `User`
- `Role`
- `Project`
- `Task`
- `ProjectMember`
- `Comment`
- `Notification`
- `Attachment`

## Быстрый запуск

Нужны Java 25, Docker Desktop и Git.

1. Запустить PostgreSQL:

```bash
docker compose up -d
```

2. Запустить приложение:

```bash
./mvnw spring-boot:run
```

Для Windows можно использовать:

```bat
mvnw.cmd spring-boot:run
```

Приложение будет доступно на `http://localhost:8080`.

При первом запуске Liquibase автоматически создаст таблицы и добавит роли `ROLE_USER` и `ROLE_ADMIN`.

## Настройки базы данных

По умолчанию проект подключается к базе из `docker-compose.yml`:

- database: `vinegaradb`
- username: `vinegara`
- password: `0000`
- port: `5432`

Если нужно подключить другую базу, можно передать переменные окружения:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vinegaradb
SPRING_DATASOURCE_USERNAME=vinegara
SPRING_DATASOURCE_PASSWORD=0000
```

## Тесты и покрытие

Запуск всех тестов и проверки покрытия:

```bash
./mvnw verify
```

Для Windows:

```bat
mvnw.cmd verify
```

Текущий результат проверки:

- tests: `39`
- failures: `0`
- errors: `0`
- instruction coverage: `78.7%`
- line coverage: `89.0%`

HTML-отчет JaCoCo после запуска находится в `target/site/jacoco/index.html`.

## Основные endpoint'ы

- `POST /api/auth/signup` - регистрация
- `POST /api/auth/signin` - вход и получение JWT
- `/api/projects` - проекты
- `/api/tasks` - задачи
- `/api/comments` - комментарии
- `/api/notifications` - уведомления
- `/api/attachments` - вложения
- `/api/project-members` - участники проектов
- `/api/admin/users` - пользователи, только admin
- `/api/admin/roles` - роли, только admin
- `/actuator/health` - состояние приложения

