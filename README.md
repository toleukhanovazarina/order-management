# Order Management Service

Order Management Service — это Spring Boot приложение для управления заказами, включающее метрики и базу данных PostgreSQL.

## Содержание

1. [Требования](#требования)
2. [Сборка и запуск](#сборка-и-запуск)
   - [С использованием Docker](#с-использованием-docker)
   - [Локальный запуск](#локальный-запуск)
3. [Использование](#использование)
4. [Метрики](#метрики)

---

## Требования

Для запуска приложения необходимо:

- Java 17+
- Maven 3.8+
- PostgreSQL 12+
- Docker** (для запуска)

---

## Сборка и запуск

### С использованием Docker

1. Соберите проект с использованием Maven:
   ```bash
   mvn clean install -DskipTests

2. Создайте образ Docker:
   ```bash
   docker build -t order-management-app .

3. Запустите собранный образ:
   ```bash
   docker run --name order-management-app -p 8080:8080 -d order-management-app

4. Проверьте статус запущенных контейнеров:
   ```bash
   docker ps
   
5. Проверьте логи запущенного образа:
   ```bash
   docker logs -f 'container-id'


### Локальный запуск

1. Настройте базу данных PostgreSQL:
    Убедитесь, что PostgreSQL работает на вашем локальном компьютере.
    Создайте базу данных ordermanagement и пользователя:
    ```bash
    CREATE DATABASE ordermanagement;
    CREATE USER admin WITH PASSWORD 'password';
    GRANT ALL PRIVILEGES ON DATABASE ordermanagement TO admin;


2. Обновите файл application.properties или используйте переменные окружения:
   ```bash
   spring.datasource.url=jdbc:postgresql://localhost:5432/order_management
   spring.datasource.username=admin
   spring.datasource.password=password

3. Запустите приложение:
   ```bash
   mvn spring-boot:run


## Использование
После запуска приложения:

Приложение доступно по адресу: http://IP-address:8080.
Вы можете использовать Postman или curl для тестирования API.


## Метрики
Приложение предоставляет метрики через эндпоинт /metrics

### Просмотр актуальных метрик:
curl http://IP-address:8080/actuator/metrics

### Просмотр метрики http.server.requests
curl http://localhost:8080/metrics
