# Проектная работа яндекс практикум, спринт 3. 

Создание бэкенд приложения-блога с использованием Spring Framework.

---

## Что внутри

- **REST-контроллер** `PostController` (`/api/posts"`)

- **DAO-слой**: `Post`, `Comment`, `Tag`, `Post_Tag`" репозитории на `JdbcTemplate`

- **База данных H2 (in-memory)**: описана в `schema.sql`

- **Тесты**:
    - пакет `service` — unit тесты сервиса
    - пакет `controller` и `repository` — интеграционные тесты контроллера и репозитория (JdbcTemplate + H2)

---

## Запуск приложения

- **Собрать проект с помощью Maven:** `mvn clean package`

- **Скопировать файл target/ROOT.war в папку:** `webapps Tomcat`

- **Запустить:** `webapps Tomcat`