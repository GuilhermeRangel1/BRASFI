# BRASFI

Plataforma web para apresentar a BRASFI e apoiar sua rede de aprendizagem, eventos e comunidades em finanças e investimentos sustentáveis.

O projeto combina uma página inicial institucional, área autenticada para participantes e ferramentas administrativas para manter conteúdos dinâmicos pelo próprio sistema.

## Stack

- Frontend: React + Vite, servido por Nginx no Docker.
- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA e WebSocket/STOMP.
- Banco de dados: MySQL 8.
- Infra local: Docker Compose.

## Estrutura

```text
BRASFI
|-- frontend/              # Aplicação React
|-- webapp/                # Backend Spring Boot
|-- docker-compose.yml     # MySQL + backend + frontend
|-- .env.example           # Variáveis de ambiente de exemplo
`-- README.md
```

## Funcionalidades

- Home institucional com navegação superior, seções de apresentação, atuação, impacto e depoimentos.
- Login e cadastro de usuários, com perfis de usuário comum, gerente e administrador.
- Página "Minha jornada" com dados do perfil e histórico do participante.
- Trilhas de aprendizagem criadas pelo administrador, com etapas, progresso do usuário e materiais opcionais por etapa.
- Upload de arquivos de apoio para trilhas, persistidos no backend.
- Eventos futuros e gravados, com criação, edição, remoção, inscrição e cancelamento de inscrição.
- Página detalhada de evento com descrição completa, convidados e vídeo incorporado quando informado.
- Comunidades com entrada e saída de membros, mensagens em tempo real, edição administrativa e moderação de posts.
- Painel administrativo visual para gerenciar eventos, trilhas e comunidades.
- Seed opcional com dados de demonstração e usuário administrador inicial.

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

## Variáveis de Ambiente

O projeto roda com valores padrão, mas você pode criar um `.env` a partir de `.env.example`:

```bash
copy .env.example .env
```

Principais variáveis:

- `FRONTEND_PORT`: porta do frontend, padrão `3000`.
- `APP_PORT`: porta do backend, padrão `8080`.
- `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_LOCAL_PORT`: configuração do MySQL.
- `SEED_DEMO_DATA`: cria dados iniciais quando `true`.
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_CPF`: credenciais do administrador inicial.
- `ALLOW_PRIVILEGED_REGISTRATION`: permite cadastro público de administrador ou gerente quando `true`.

## Usuário Inicial

Com `SEED_DEMO_DATA=true`, o sistema cria um administrador para testes locais:

```text
email: admin@brasfi.com
senha: admin123
```

Altere essas credenciais no `.env` antes de usar fora de ambiente local.

## API

Endpoints principais:

- `GET /api/v1/dashboard`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/register`
- `GET /api/v1/learning-tracks`
- `POST /api/v1/learning-tracks`
- `PUT /api/v1/learning-tracks/{id}`
- `DELETE /api/v1/learning-tracks/{id}`
- `POST /api/v1/learning-tracks/{id}/progress`
- `POST /api/v1/learning-tracks/{id}/materials`
- `GET /api/v1/events`
- `GET /api/v1/events/{id}`
- `POST /api/v1/events`
- `PUT /api/v1/events/{id}`
- `DELETE /api/v1/events/{id}`
- `POST /api/v1/events/{id}/registrations`
- `DELETE /api/v1/events/{id}/registrations`
- `GET /api/v1/communities`
- `POST /api/v1/communities`
- `PUT /api/v1/communities/{id}`
- `DELETE /api/v1/communities/{id}`
- `POST /api/v1/communities/{id}/membership`
- `DELETE /api/v1/communities/{id}/membership`
- `GET /api/v1/communities/{id}/posts`
- `POST /api/v1/communities/{id}/posts`
- `DELETE /api/v1/communities/{communityId}/posts/{postId}`

As rotas de criação, edição, remoção e moderação exigem usuário autenticado com permissão administrativa.

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

## Persistência Local

O Docker Compose cria volumes para:

- Dados do MySQL: `db_data`.
- Arquivos enviados pela aplicação: `uploads_data`.

Para reiniciar a aplicação mantendo os dados, use apenas `docker compose down`. Para apagar banco e uploads locais, remova também os volumes.

## Autores

- [Arthur Xavier](https://github.com/arthurxavi)
- [Felipe Andrade](https://github.com/felipeandrader)
- [Guilherme Vinicius](https://github.com/GuilhermeRangel1)
- [João Robalinho](https://github.com/JRobalinho)
- [Pedro Gusmão](https://github.com/pedroguswander)
