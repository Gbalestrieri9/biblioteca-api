# Biblioteca Online API

Sistema de biblioteca online desenvolvido para o **Tech Challenge — Fase 2
(Substitutiva) — Arquitetura e Desenvolvimento Java**, da Pós Tech (FIAP).

API REST para cadastro de livros e usuários, controle de empréstimos e
devoluções, consulta ao acervo com filtros e paginação, reservas e
relatórios de uso da biblioteca — protegida por autenticação JWT Bearer.

## Stack técnica

| Camada            | Tecnologia                                      |
|-------------------|--------------------------------------------------|
| Linguagem         | Java 21                                          |
| Framework         | Spring Boot 3.3 (Web, Validation, Actuator)      |
| Autenticação      | Spring Security + JWT (jjwt)                     |
| Persistência      | Spring Data JPA / Hibernate                      |
| Banco de dados    | PostgreSQL 16                                    |
| Migrations        | Flyway                                           |
| Documentação      | springdoc-openapi (Swagger UI) + Javadoc         |
| Testes            | JUnit 5 + Mockito + AssertJ                      |
| Containerização   | Docker + Docker Compose                          |
| Build             | Maven                                            |

## Arquitetura

O projeto segue uma arquitetura em camadas (**controller → service →
repository**), com DTOs para desacoplar o contrato da API do modelo de
persistência:

```
com.postech.biblioteca
├── config          # Constantes, OpenAPI, utilitário de paginação
├── controller       # Endpoints REST
├── dto
│   ├── request       # Dados de entrada validados com Bean Validation
│   └── response       # Dados de saída (records imutáveis)
├── entity            # Entidades JPA
├── enums             # StatusEmprestimo, StatusReserva, Papel
├── exception         # Exceções de negócio + tratamento global
├── mapper            # Conversão entidade <-> DTO
├── repository
│   ├── projection     # Projeções para relatórios
│   └── specification  # Filtros dinâmicos (JPA Specification)
├── security          # Autenticação JWT (filtro, geração/validação de token, UserDetailsService)
└── service
    └── impl           # Regras de negócio
```

Padrões aplicados: **DTO**, **Repository**, **Specification** (filtros
dinâmicos e combináveis), **Builder** (via Lombok), injeção de dependência
por construtor e `@ControllerAdvice` para tratamento centralizado de erros.

## Convenção de data e hora

Todo o sistema opera em **UTC (GMT+0)**. Datas com hora
(`dataEmprestimo`, `dataDevolucao`) são `Instant`, serializadas em
ISO-8601 (`2026-09-02T14:30:00Z`). A data prevista de devolução é um
`LocalDate` (`yyyy-MM-dd`), calculada com a API `java.time` somando **14
dias corridos** à data do empréstimo. Ver `AppConstants` para o ponto único
de configuração do fuso e do prazo.

## Autenticação (JWT Bearer Token)

A API exige um token JWT (`Authorization: Bearer <token>`) em quase todos
os endpoints. A autenticação é **stateless** — nenhuma sessão fica guardada
no servidor, o próprio token carrega a identidade (e-mail) e o papel do
usuário a cada requisição, o que permite escalar a API horizontalmente sem
sticky sessions.

### Papéis de acesso

| Papel            | Pode acessar                                                              |
|-------------------|----------------------------------------------------------------------------|
| `BIBLIOTECARIO`   | Tudo — inclusive cadastro/atualização/exclusão de livros e usuários       |
| `USUARIO`         | Todo o restante (consultas, empréstimos, devoluções, reservas, relatórios, `/auth/me`) — sem acesso à gestão de acervo/usuários |

Essas regras ficam centralizadas em `SecurityConfig` (por rota + método
HTTP), não espalhadas pelos controllers.

### Endpoints de autenticação

| Método | Caminho                    | Acesso   | Descrição                                                  |
|--------|-----------------------------|----------|-----------------------------------------------------------|
| POST   | `/api/v1/auth/login`        | Público  | Autentica com e-mail/senha, retorna o token Bearer         |
| POST   | `/api/v1/auth/registrar`    | Público  | Autocadastro de um novo usuário (sempre papel `USUARIO`)   |
| GET    | `/api/v1/auth/me`           | Autenticado | Dados do usuário autenticado (a partir do token)        |

### Usuário de demonstração

A migration `V3__add_autenticacao.sql` cria um usuário `BIBLIOTECARIO` de
demonstração, necessário para usar os endpoints administrativos assim que a
aplicação sobe:

```
E-mail: admin@biblioteca.com
Senha:  Admin@123
```

**Troque essa senha (ou remova o usuário) em qualquer ambiente real.** Os
usuários que já existiam antes da autenticação (seed `V2__seed_data.sql`:
Ana, Bruno, Carla) também recebem, na mesma migration, uma senha padrão e
temporária (`Mudar@123`, papel `USUARIO`) — troque-a antes de usar essas
contas para login.

### Como obter e usar o token

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@biblioteca.com", "senha": "Admin@123"}'

# Resposta:
# {"accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tipo": "Bearer", "expiraEmSegundos": 86400}

# 2. Use o token nas demais chamadas
curl http://localhost:8080/api/v1/livros \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

Sem o cabeçalho `Authorization` (ou com um token inválido/expirado), rotas
que exigem autenticação respondem `401`; uma rota que exige `BIBLIOTECARIO`
acessada com um token `USUARIO` responde `403`.

### Segredo de assinatura do token (`JWT_SECRET`)

O segredo usado para assinar/validar os tokens (HMAC-SHA256) é configurável
via variável de ambiente `JWT_SECRET` (ver `application.yml` e
`docker-compose.yml`) — o valor padrão embutido serve **só para
desenvolvimento local**. Em qualquer ambiente real, defina uma string
aleatória de pelo menos 32 caracteres:

```bash
export JWT_SECRET=<uma string aleatória de pelo menos 32 caracteres>
```

O tempo de expiração do token (`JWT_EXPIRACAO_MS`, padrão 24h em
milissegundos) também é configurável do mesmo jeito.

## Como executar

### Opção 1 — Docker (recomendado)

Pré-requisitos: Docker e Docker Compose.

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080` e o PostgreSQL em `localhost:5432`.
As tabelas, os dados de exemplo e o usuário `BIBLIOTECARIO` de demonstração
são criados automaticamente pelo Flyway na primeira subida.

Para derrubar tudo (incluindo os dados persistidos):

```bash
docker compose down -v
```

### Opção 2 — Local (Maven + PostgreSQL já instalado)

```bash
# suba um PostgreSQL local (ou use o docker compose só para o banco)
docker compose up -d db

export DB_HOST=localhost DB_PORT=5432 DB_NAME=biblioteca DB_USER=biblioteca DB_PASSWORD=biblioteca

mvn spring-boot:run
```

Opcionalmente, defina seu próprio segredo JWT (senão o valor padrão do
`application.yml` é usado):

```bash
export JWT_SECRET=<uma string aleatória de pelo menos 32 caracteres>
```

### Rodando os testes

```bash
mvn test
```

## Documentação da API

Com a aplicação em execução:

* **Swagger UI:** http://localhost:8080/swagger-ui.html
* **OpenAPI JSON:** http://localhost:8080/v3/api-docs
* **Health check:** http://localhost:8080/actuator/health

O Swagger UI já vem com o esquema `bearerAuth` configurado — use o botão
**Authorize** para informar o token e testar os endpoints protegidos
diretamente pela interface.

Javadoc pode ser gerado com:

```bash
mvn javadoc:javadoc
# saída em target/site/apidocs/index.html
```

## Testando com Postman

A pasta [`postman/`](postman/) contém uma collection completa
(`Biblioteca-Online-API.postman_collection.json`) que exercita o fluxo de
ponta a ponta — autenticação, cadastro de livros/usuários, empréstimo,
devolução, reservas, relatórios e cenários de erro/validação/autorização
(401/403) — com asserts automáticos e injeção automática do token Bearer.
Basta importar no Postman e rodar via **Collection Runner**; veja
[`postman/README.md`](postman/README.md) para o passo a passo.

## Principais endpoints

| Recurso      | Método | Caminho                                   | Acesso              | Descrição                                   |
|--------------|--------|--------------------------------------------|---------------------|------------------------------------------------|
| Auth         | POST   | `/api/v1/auth/login`                       | Público             | Login, retorna token Bearer                   |
|              | POST   | `/api/v1/auth/registrar`                   | Público             | Autocadastro (papel USUARIO)                  |
|              | GET    | `/api/v1/auth/me`                          | Autenticado         | Dados do usuário autenticado                  |
| Livros       | POST   | `/api/v1/livros`                           | BIBLIOTECARIO       | Cadastra um livro                             |
|              | POST   | `/api/v1/livros/lote`                      | BIBLIOTECARIO       | Cadastra vários livros em lote                |
|              | GET    | `/api/v1/livros?titulo=&autor=&isbn=&page=`| Autenticado         | Busca paginada com filtros                    |
|              | GET    | `/api/v1/livros/{id}`                      | Autenticado         | Busca por id                                  |
|              | PUT    | `/api/v1/livros/{id}`                      | BIBLIOTECARIO       | Atualiza                                      |
|              | DELETE | `/api/v1/livros/{id}`                      | BIBLIOTECARIO       | Remove                                        |
| Usuários     | POST   | `/api/v1/usuarios`                         | BIBLIOTECARIO       | Cadastra um usuário (escolhe nome/e-mail/senha/papel) |
|              | GET    | `/api/v1/usuarios?nome=&page=`             | Autenticado         | Busca paginada                                |
|              | GET    | `/api/v1/usuarios/{id}/emprestimos`        | Autenticado         | Histórico de empréstimos                      |
|              | PUT    | `/api/v1/usuarios/{id}`                    | BIBLIOTECARIO       | Atualiza                                      |
|              | DELETE | `/api/v1/usuarios/{id}`                    | BIBLIOTECARIO       | Remove                                        |
| Empréstimos  | POST   | `/api/v1/emprestimos`                      | Autenticado         | Registra empréstimo                           |
|              | PUT    | `/api/v1/emprestimos/{id}/devolucao`       | Autenticado         | Registra devolução                            |
|              | GET    | `/api/v1/emprestimos?status=&usuarioId=`   | Autenticado         | Lista paginada                                |
| Reservas     | POST   | `/api/v1/reservas`                         | Autenticado         | Reserva um livro indisponível                 |
|              | PUT    | `/api/v1/reservas/{id}/cancelamento`       | Autenticado         | Cancela reserva                               |
|              | GET    | `/api/v1/reservas?usuarioId=`              | Autenticado         | Lista paginada                                |
| Relatórios   | GET    | `/api/v1/relatorios/livros-mais-emprestados` | Autenticado        | Top 20 livros mais emprestados              |
|              | GET    | `/api/v1/relatorios/emprestimos-ativos`    | Autenticado         | Empréstimos ativos + previsão de devolução    |

Exemplos completos de request/response estão disponíveis no Swagger UI.

## Licença

Projeto acadêmico — Tech Challenge Pós Tech FIAP.
