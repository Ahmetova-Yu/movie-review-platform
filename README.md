# Movie Review Platform — Микросервисная платформа для рецензирования фильмов

Платформа для управления фильмами и пользовательскими рецензиями с авторизацией, асинхронным обновлением рейтингов и многопоточным парсингом.

**Асинхронное взаимодействие:** Review Service → Kafka → Film Service (обновление рейтинга)

---

## Технологический стек

| Категория | Технологии |
|-----------|------------|
| **Язык** | Java 17 |
| **Фреймворки** | Spring Boot 3, Spring Cloud Gateway, Spring Data JPA, Spring Kafka |
| **Базы данных** | PostgreSQL, Redis |
| **Брокер** | Apache Kafka |
| **Авторизация** | JWT (Auth0 / JJWT) |
| **Тестирование** | JUnit 5, Mockito, Testcontainers, MockMvc |
| **Сборка** | Maven |
| **Контейнеризация** | Docker, docker-compose |
| **Логирование** | SLF4J, Lombok |

## Микросервисы

| Сервис | Порт | База данных | Основная задача |
|--------|------|-------------|-----------------|
| **API Gateway** | 8080 | — | Маршрутизация, JWT-валидация |
| **User Service** | 8081 | PostgreSQL | Регистрация, логин, генерация JWT |
| **Film Service** | 8082 | PostgreSQL + Redis | CRUD фильмов, кэширование, Kafka consumer |
| **Review Service** | 8083 | PostgreSQL | Рецензии, лайки, статистика, Kafka producer |
| **Parsing Service** | 8084 | — | Многопоточный парсинг фильмов, Kafka producer |
