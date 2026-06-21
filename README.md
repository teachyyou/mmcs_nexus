# MMCS Nexus

> Language: [English](README.md) | [Русский](README.ru.md)

MMCS Nexus is an educational web platform for automating project activity workflows at the Institute of Mathematics, Mechanics, and Computer Science. The system provides a unified environment for administrators, jury members, and students, replacing scattered spreadsheets, forms, and file storage with a role-based web application.

The platform supports project management, reporting stages, jury assignments, grading, project submissions, file uploads, and public information posts. It was developed as an academic project using Spring Boot, React, PostgreSQL, and Docker.

## Live Deployment

The current deployed version is available at:

https://nexus.tetrerge.ru/

> Note: the deployment may require a VPN when accessed from Russia.

## Deployment with Docker

The application can be deployed using prebuilt Docker images from GitHub Container Registry.

Published images:

```text
ghcr.io/teachyyou/mmcs-nexus-backend:latest
ghcr.io/teachyyou/mmcs-nexus-client:latest
````

The images are public, so no `docker login ghcr.io` is required for pulling them.

### Pull Images Manually

```sh
docker pull ghcr.io/teachyyou/mmcs-nexus-backend:latest
docker pull ghcr.io/teachyyou/mmcs-nexus-client:latest
```

## Environment Variables

Create a `.env` file in the project root.

Example:

```env
POSTGRES_USER=nexus
POSTGRES_PASSWORD=nexus_password

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

PORT=3000
BASE_URL=http://localhost:3000

POSTS_UPLOAD_PATH=./uploads
```

For Windows, the upload path may be specified as an absolute path:

```env
POSTS_UPLOAD_PATH=C:\mmcs_nexus\uploads
```

## GitHub OAuth Setup

To use GitHub authentication, create a GitHub OAuth App.

Required values:

* `GITHUB_CLIENT_ID`
* `GITHUB_CLIENT_SECRET`
* callback URL matching the application base URL used in deployment

For local Docker deployment, the base URL is usually:

```text
http://localhost:3000
```

## Docker Compose Deployment

A `docker-compose` configuration is provided in the repository. It starts the application using three main services:

* `postgres` — PostgreSQL database
* `backend` — Spring Boot backend
* `client` — React frontend

Example configuration using published images:

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

### Start the Application

From the repository root:

```sh
docker compose up -d
```

Then open:

```text
http://localhost:3000
```

### Stop the Application

```sh
docker compose down
```

```
