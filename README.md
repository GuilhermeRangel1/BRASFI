# ☀️ Projeto BRASFI - Plataforma de Financiamentos Sustentáveis

## 📖 Visão Geral do Projeto

A BRASFI (Brasil Sustentável em Finanças e Investimentos) é uma aliança de pesquisadores fundada em abril de 2020, com a missão de construir e compartilhar conhecimento sobre finanças e investimentos sustentáveis. Nosso projeto visa criar uma plataforma digital que serve como um ecossistema financeiro responsável, divulgando eventos sobre financiamentos sustentáveis, conectando uma comunidade engajada e facilitando a comunicação entre seus membros.

Com uma abordagem multidisciplinar, a BRASFI reúne professores, pesquisadores, alunos de graduação, mestrado e doutorado, além de especialistas do mercado, combinando experiências acadêmicas e práticas para impulsionar o desenvolvimento sustentável no setor financeiro.

## 🌱 Objetivos da Plataforma

Nossa missão é criar um ecossistema financeiro mais responsável, ajudando nossos usuários a atingirem seus objetivos financeiros enquanto cuidam do futuro do nosso planeta.

* **Inovação**: Implementamos as melhores soluções financeiras com práticas verdes.
* **Responsabilidade Social**: Divulgamos e investimos em empresas e projetos que têm um impacto positivo no meio ambiente.
* **Transparência e Ética**: Garantimos que nossos processos sejam claros e acessíveis a todos os envolvidos.

## 🚀 Funcionalidades Principais

A plataforma oferece as seguintes funcionalidades:

* **Autenticação e Autorização**: Sistema de login e controle de acesso com perfis de **Usuário Comum** e **Administrador**.
* **Cadastro de Usuários**: Formulário para novos usuários se registrarem na plataforma.
* **Gestão de Eventos**:
    * Visualização de eventos futuros (Agenda de Eventos).
    * Visualização de eventos gravados (Eventos Gravados) com opções de filtragem por categoria.
    * **Funcionalidades de Administrador**: Criação, edição e exclusão de eventos.
* **Comunidades**:
    * **Participação em Comunidades**: Usuários podem entrar em diversas comunidades de interesse.
    * **Troca de Mensagens**: Possibilidade de enviar e receber mensagens dentro das comunidades para interagir com outros membros.
* **Conteúdo Informativo**: Páginas dedicadas a apresentar a empresa (Sobre a BRASFI), e informações gerais (Home).

## ⚙️ Tecnologias Utilizadas

Este projeto foi desenvolvido utilizando as seguintes tecnologias:

* **Backend**:
    * ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) **Java 17+**: Linguagem de programação principal.
    * ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) **Spring Boot 3.x**: Framework para construção de aplicações Java.
    * ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) **Spring Security 6.x**: Para autenticação e autorização robustas.
    * ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring-data-jpa&logoColor=white) **Spring Data JPA / Hibernate**: Para persistência de dados.
    * ![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white) **Maven**: Ferramenta de gerenciamento de dependências e construção de projetos.
* **Frontend**:
    * ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white) **HTML5**
    * ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white) **CSS3**
    * ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black) **JavaScript**
    * ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white) **Thymeleaf**: Motor de templates para integração com o Spring Boot.
    * ![Font Awesome](https://img.shields.io/badge/Font_Awesome-528DD7?style=for-the-badge&logo=font-awesome&logoColor=white) **Font Awesome**: Ícones.
* **Banco de Dados**:
    * ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) **MySQL**: Sistema de gerenciamento de banco de dados relacional.
* **Containers**:
    * ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) **Docker** e **Docker Compose**: Para conteinerização da aplicação e do banco de dados, facilitando o ambiente de desenvolvimento.

## ⚙️ Como Rodar o Projeto Localmente

Siga estas instruções para configurar e executar o projeto em sua máquina local. Você tem duas opções para iniciar a aplicação:

### Pré-requisitos Comuns

Certifique-se de ter as seguintes ferramentas instaladas:

* **Docker Desktop**: Essencial para executar o contêiner do banco de dados MySQL e, opcionalmente, o contêiner da aplicação.

### Passos de Configuração

1.  **Clone o Repositório:**
    ```bash
    git clone [URL_DO_SEU_REPOSITORIO]
    cd [NOME_DA_PASTA_DO_PROJETO]
    ```
    *(Substitua `[URL_DO_SEU_REPOSITORIO]` e `[NOME_DA_PASTA_DO_PROJETO]` pelo caminho real do seu projeto.)*

2.  **Verificar e Configurar o Banco de Dados no `docker-compose.yml`:**
    Abra o arquivo `docker-compose.yml` na raiz do projeto.
    Certifique-se de que as variáveis de ambiente para o serviço `mysql` (como `MYSQL_ROOT_PASSWORD` e `MYSQL_DATABASE`) estão configuradas conforme desejado.

    **Exemplo da seção `mysql` no `docker-compose.yml`:**

    ```yaml
    services:
      mysql:
        image: mysql:8.0 # Ou a versão que você estiver usando
        environment:
          MYSQL_ROOT_PASSWORD: root         # Senha do usuário root (para conexão root)
          MYSQL_DATABASE: brasfi_db         # Nome do banco de dados que será criado
          # Outras variáveis, se houver (ex: MYSQL_USER, MYSQL_PASSWORD para um usuário não-root)
        ports:
          - "3306:3306" # Mapeia a porta do contêiner para a porta da sua máquina local
        # ... outras configurações do MySQL, se houver
    ```

---

### Opção 1: Rodar Totalmente Conteinerizado (Recomendado)

Esta opção utiliza Docker Compose para construir sua aplicação Java dentro de um contêiner e orquestrá-la junto com o banco de dados.

**Pré-requisitos Adicionais para esta opção:** Nenhum. (O Dockerfile da sua aplicação já deve cuidar do Java/Maven).

1.  **Verificar as Configurações da Aplicação (`application.properties/yml`) para ambiente conteinerizado:**
    Abra o arquivo `src/main/resources/application.properties` (ou `application.yml`).
    Certifique-se de que os detalhes de conexão com o banco de dados estão configurados para se comunicar com o contêiner MySQL.

    **Exemplo do que você deve ver e verificar em `application.properties`:**

    ```properties
    # Conexão com o MySQL (host 'mysql' é o nome do serviço no docker-compose.yml)
    spring.datasource.url=jdbc:mysql://mysql:3306/brasfi_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    spring.datasource.username=root
    spring.datasource.password=root

    # Outras configurações JPA/Hibernate (manter como está)
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
    ```
    *Dica: O `spring.datasource.url` usa `mysql:3306` porque sua aplicação estará em um contêiner Docker e `mysql` é o nome do serviço do banco de dados dentro da rede Docker Compose. O nome do banco (`brasfi_db`), usuário (`root`) e senha (`root`) devem ser os mesmos que você configurou no seu `docker-compose.yml`.*

2.  **Construir e Iniciar a Aplicação com Docker Compose:**
    Na pasta raiz do projeto (onde estão o `docker-compose.yml` e o `Dockerfile` da sua aplicação), execute o seguinte comando:
    ```bash
    docker-compose up --build
    ```
    Este comando irá:
    * Construir a imagem Docker da sua aplicação (usando o `Dockerfile`).
    * Criar e iniciar os contêineres para o MySQL e para a sua aplicação.
    * Ligar a aplicação ao banco de dados.

    A aplicação estará acessível no seu navegador em `http://localhost:8080` (ou a porta que você configurou para sua aplicação no `docker-compose.yml`).
    Para parar e remover os contêineres: `docker-compose down`.

---

### Opção 2: Rodar com Maven (Local) e Docker para o Banco de Dados

Esta opção permite que você execute a aplicação Spring Boot diretamente em sua máquina local enquanto o banco de dados MySQL é executado em um contêiner Docker.

**Pré-requisitos Adicionais para esta opção:**
* **Java Development Kit (JDK)**: Versão 17 ou superior.
* **Apache Maven**: Versão 3.x ou superior.

1.  **Iniciar Apenas o Contêiner do Banco de Dados:**
    Na pasta raiz do projeto, execute:
    ```bash
    docker-compose up -d mysql
    ```
    *Este comando iniciará apenas o serviço MySQL em segundo plano. Aguarde alguns segundos para o banco de dados estar totalmente operacional antes de iniciar a aplicação.*

2.  **Verificar as Configurações da Aplicação (`application.properties/yml`) para ambiente local:**
    Abra o arquivo `src/main/resources/application.properties` (ou `application.yml`).
    Certifique-se de que os detalhes de conexão com o banco de dados estão configurados para se conectar ao MySQL rodando no `localhost`.

    **Exemplo do que você deve ver e verificar em `application.properties`:**

    ```properties
    # Conexão com o MySQL (host 'localhost' para ambiente local)
    spring.datasource.url=jdbc:mysql://localhost:3306/brasfi_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    spring.datasource.username=root
    spring.datasource.password=root

    # Outras configurações JPA/Hibernate (manter como está)
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
    ```
    *Dica: O `spring.datasource.url` usa `localhost:3306` porque sua aplicação estará rodando diretamente na sua máquina, e o Docker mapeia a porta 3306 do contêiner para a 3306 do `localhost`. O nome do banco (`brasfi_db`), usuário (`root`) e senha (`root`) devem ser os mesmos que você configurou no seu `docker-compose.yml`.*

3.  **Compilar e Executar a Aplicação Spring Boot com Maven:**
    Na pasta raiz do projeto (onde está o `pom.xml`), execute os seguintes comandos:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    A aplicação estará acessível no seu navegador em `http://localhost:8080` (ou a porta configurada no seu `application.properties`).
    Para parar apenas o banco de dados: `docker-compose down mysql`.

---

### Usuários para Teste

Para testar as funcionalidades de usuário e administrador:

1.  Acesse a página de registro: `http://localhost:8080/register`
2.  Preencha o formulário e selecione o **Tipo de Conta** desejado (`Usuário Comum` ou `Administrador`).
3.  As credenciais serão criptografadas automaticamente ao registrar.
4.  Após o registro, você poderá fazer login na página `http://localhost:8080/login`.

## 📚 Diagrama de Classes

* [Diagrama de Classes UML Contínuo - SR1 e SR2](https://lucid.app/lucidchart/f6cb28ee-c956-4cb7-9649-154ae0e29656/edit?viewport_loc=-7468%2C-9705%2C4377%2C2210%2C0_0&invitationId=inv_acaf2bc2-96c4-4108-922d-b1287a485501)

## 👨‍💻 Criado por:

* [Arthur Xavier](https://github.com/arthurxavi)
* [Felipe Andrade](https://github.com/felipeandrader)
* [Guilherme Vinicius](https://github.com/GuilhermeRangel1)
* [João Robalinho](https://github.com/JRobalinho)
* [Pedro Gusmão](https://github.com/pedroguswander)