# Система автоматизации сервисного центра по ремонту ноутбуков

Выпускная квалификационная работа бакалавра МАИ, направление «Прикладная математика и информатика».

## О проекте

Микросервисная система для автоматизации бизнес-процессов сервисного центра, таких как управление заказами, складской учёт запчастей, гарантийное обслуживание, сбор финансовой статистики.

## Технологии

- **Backend:** Java 17, Spring Boot, Spring Cloud Gateway, Spring Data JPA
- **Базы данных:** PostgreSQL (5 независимых БД), Flyway (миграции)
- **Брокер сообщений:** Apache Kafka + паттерн Transactional Outbox
- **Безопасность:** JWT (RS256), refresh-токены (ротация, HttpOnly-cookie, привязка к браузеру)
- **Отказоустойчивость:** Resilience4j (Circuit Breaker, Retry, Timeout)
- **Наблюдаемость:** Micrometer Tracing, Zipkin, SLF4J/Logback
- **Инфраструктура:** Docker, Docker Compose
- **Тестирование:** JUnit 5, Mockito, MockMvc, DataJpaTest, H2

## Запуск

```bash
# 1. Клонировать репозиторий
git clone https://github.com/DmitriyShutov1/repair-system.git
cd repair-system

# 2. Запустить все контейнеры
docker-compose up -d

# 3. Запустить веб-интерфейс
cd front
npm install
npm start
