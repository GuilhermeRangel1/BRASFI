# BRASFI

Plataforma web para o ecossistema BRASFI, com agenda de eventos, eventos gravados, comunidades e area autenticada para gestao de conteudo.

## Stack

- Frontend: React + Vite, servido por Nginx.
- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA e WebSocket/STOMP.
- Banco de dados: MySQL 8.
- Infra local: Docker Compose.

## Estrutura

```text
BRASFI
|-- frontend/              # Aplicacao React
|-- webapp/                # Backend Spring Boot
|-- docker-compose.yml     # MySQL + backend + frontend
|-- .env.example           # Variaveis de ambiente de exemplo
`-- README.md
```

## Funcionalidades

- Painel inicial com resumo de eventos e comunidades.
- Listagem de eventos futuros e gravados.
- Criacao de eventos por administradores.
- Comunidades com postagens e atualizacao via WebSocket.
- Login, cadastro de usuario comum e controle basico de permissoes.
- Seed opcional com dados de demonstracao e usuario administrador.

## Como Rodar

Requisito: Docker Desktop.

Na raiz do projeto:

```bash
docker compose up --build
```

Acesse:

- Frontend: http://localhost:3000
- Backend/API: http://localhost:8080
- MySQL local: `localhost:3307`

Para parar:

```bash
docker compose down
```

## Variaveis de Ambiente

O projeto roda com valores padrao, mas voce pode criar um `.env` a partir de `.env.example`:

```bash
copy .env.example .env
```

Principais variaveis:

- `FRONTEND_PORT`: porta do frontend, padrao `3000`.
- `APP_PORT`: porta do backend, padrao `8080`.
- `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_LOCAL_PORT`: configuracao do MySQL.
- `SEED_DEMO_DATA`: cria dados iniciais quando `true`.
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_CPF`: credenciais do admin inicial.
- `ALLOW_PRIVILEGED_REGISTRATION`: permite cadastro publico de admin/manager quando `true`.

## Usuario Inicial

Com `SEED_DEMO_DATA=true`, o sistema cria:

```text
email: admin@brasfi.com
senha: admin123
```

Altere essas credenciais no `.env` antes de usar fora de ambiente local.

## API

Endpoints principais:

- `GET /api/v1/dashboard`
- `GET /api/v1/events`
- `POST /api/v1/events` somente admin
- `GET /api/v1/communities`
- `GET /api/v1/communities/{id}/posts`
- `POST /api/v1/communities/{id}/posts` autenticado
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/register`

## Desenvolvimento Local

Backend:

```bash
cd webapp
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Se rodar o frontend via Vite, mantenha o backend ativo em `localhost:8080`.

## Autores

- [Arthur Xavier](https://github.com/arthurxavi)
- [Felipe Andrade](https://github.com/felipeandrader)
- [Guilherme Vinicius](https://github.com/GuilhermeRangel1)
- [Joao Robalinho](https://github.com/JRobalinho)
- [Pedro Gusmao](https://github.com/pedroguswander)
