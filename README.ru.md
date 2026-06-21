# MMCS Nexus

> Язык: [English](README.md) | [Русский](README.ru.md)

MMCS Nexus — учебная веб-платформа для автоматизации процессов проектной деятельности в Институте математики, механики и компьютерных наук. Система предоставляет единую среду для администраторов, членов жюри и студентов, заменяя разрозненные электронные таблицы, формы и файловые хранилища ролевым веб-приложением.

Платформа поддерживает управление проектами, этапами отчётности, назначениями жюри, выставлением оценок, отправкой отчётных материалов, загрузкой файлов и публикацией информационных постов. Проект разработан как учебная работа с использованием Spring Boot, React, PostgreSQL и Docker.

## Рабочий деплой

Текущая развёрнутая версия доступна по адресу:

https://nexus.tetrerge.ru/

> Примечание: при доступе из России для открытия сайта может потребоваться VPN.

## Развёртывание через Docker

Приложение можно развернуть с помощью готовых Docker-образов, опубликованных в GitHub Container Registry.

Опубликованные образы:

```text
ghcr.io/teachyyou/mmcs-nexus-backend:latest
ghcr.io/teachyyou/mmcs-nexus-client:latest
````

Образы публичные, поэтому для их загрузки не требуется выполнять `docker login ghcr.io`.

### Ручная загрузка образов

```sh
docker pull ghcr.io/teachyyou/mmcs-nexus-backend:latest
docker pull ghcr.io/teachyyou/mmcs-nexus-client:latest
```

## Переменные окружения

Создайте файл `.env` в корне проекта.

Пример:

```env
POSTGRES_USER=nexus
POSTGRES_PASSWORD=nexus_password

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

PORT=3000
BASE_URL=http://localhost:3000

POSTS_UPLOAD_PATH=./uploads
```

Для Windows путь к директории загрузок можно указать как абсолютный путь:

```env
POSTS_UPLOAD_PATH=C:\mmcs_nexus\uploads
```

## Настройка GitHub OAuth

Для авторизации через GitHub необходимо создать GitHub OAuth App.

Необходимые значения:

* `GITHUB_CLIENT_ID`
* `GITHUB_CLIENT_SECRET`
* callback URL, соответствующий базовому URL приложения, используемому при развёртывании

Для локального развёртывания через Docker базовый URL обычно выглядит так:

```text
http://localhost:3000
```

## Развёртывание через Docker Compose

В репозитории есть конфигурация `docker-compose`. Она запускает приложение с помощью трёх основных сервисов:

* `postgres` — база данных PostgreSQL
* `backend` — серверная часть на Spring Boot
* `client` — клиентская часть на React

Пример конфигурации с использованием опубликованных образов:

```yaml
services:
  postgres:
    image: postgres:alpine3.16
    container_name: nexus_postgres
    hostname: postgres
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: mmcsnexus
    ports:
      - "5432:5432"
    volumes:
      - db-data:/var/lib/postgresql/data

  backend:
    image: ghcr.io/teachyyou/mmcs-nexus-backend:latest
    container_name: nexus_backend
    hostname: backend
    environment:
      POSTGRES_URL: postgres:5432
      POSTGRES_DATABASE: mmcsnexus
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      GITHUB_CLIENT_ID: ${GITHUB_CLIENT_ID}
      GITHUB_CLIENT_SECRET: ${GITHUB_CLIENT_SECRET}
      BASE_URL: ${BASE_URL}
      UPLOADS_DIR: /data/uploads
    volumes:
      - ${POSTS_UPLOAD_PATH}:/data/uploads
    depends_on:
      - postgres

  client:
    image: ghcr.io/teachyyou/mmcs-nexus-client:latest
    container_name: nexus_client
    environment:
      PORT: ${PORT}
      BASE_URL: ${BASE_URL}
    ports:
      - "${PORT}:3000"
    depends_on:
      - backend

volumes:
  db-data:
```

### Запуск приложения

Из корня репозитория:

```sh
docker compose up -d
```

После запуска откройте:

```text
http://localhost:3000
```

### Остановка приложения

```sh
docker compose down
```
