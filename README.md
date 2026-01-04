# Проектная работа яндекс практикум, спринт 3. 

Обновленное бэкенд приложение-блога с использованием SpringBoot и Gradle.

---

## Что изменилось

- **Используется SpringBoot**, вместо Springframework

- **Изменена система сборки приложения на Gradle**. Ранее была Maven

- **Встроенный сервер Tomcat**, вместо сборки .war

---

## Что внутри

- **REST-контроллеры** `PostController`, `CommentController` и `ImageController` (`/api/posts"`)

- **DAO-слой**: `Post`, `Comment`, `Tag`, `Post_Tag`" репозитории на `JdbcTemplate`

- **База данных H2 (in-memory)**: описана в `schema.sql`

- **Тесты**:
    - пакет `service` — unit тесты сервиса
    - пакет `controller` и `repository` — интеграционные и WebMvc тесты контроллера и репозитория (JdbcTemplate + H2)

---

## Запуск приложения

- **Сборка проекта:** `./gradlew build`

- **Запуск:** `./gradlew bootRun`

- **Приложение будет доступно по адресу:** `http://localhost:8080`