# Biblioteca Online API

Sistema de biblioteca online desenvolvido para o **Tech Challenge — Fase 2
(Substitutiva) — Arquitetura e Desenvolvimento Java**, da Pós Tech (FIAP).

API REST para cadastro de livros e usuários, controle de empréstimos e
devoluções, consulta ao acervo com filtros e paginação, reservas e
relatórios de uso da biblioteca.

## Stack técnica

| Camada            | Tecnologia                                      |
|-------------------|--------------------------------------------------|
| Linguagem         | Java 21                                          |
| Framework         | Spring Boot 3.3 (Web, Validation, Actuator)      |
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
├── enums             # StatusEmprestimo, StatusReserva
├── exception         # Exceções de negócio + tratamento global
├── mapper            # Conversão entidade <-> DTO
├── repository
│   ├── projection     # Projeções para relatórios
│   └── specification  # Filtros dinâmicos (JPA Specification)
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

## Como executar

### Opção 1 — Docker (recomendado)

Pré-requisitos: Docker e Docker Compose.

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080` e o PostgreSQL em `localhost:5432`.
As tabelas e os dados de exemplo são criados automaticamente pelo Flyway na
primeira subida.

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

### Rodando os testes

```bash
mvn test
```

> **Nota sobre o ambiente de geração deste projeto:** o código foi escrito e
> revisado manualmente neste ambiente, mas o ambiente de desenvolvimento
> usado para gerar este projeto não tinha acesso ao Maven Central para
> baixar as dependências e compilar. Ao rodar `mvn clean install` na sua
> máquina (com acesso normal à internet) as dependências serão baixadas
> normalmente. Revise o `mvn test` e `mvn clean package` como primeiro passo
> antes de seguir para o Docker.

## Documentação da API

Com a aplicação em execução:

* **Swagger UI:** http://localhost:8080/swagger-ui.html
* **OpenAPI JSON:** http://localhost:8080/v3/api-docs
* **Health check:** http://localhost:8080/actuator/health

Javadoc pode ser gerado com:

```bash
mvn javadoc:javadoc
# saída em target/site/apidocs/index.html
```

## Principais endpoints

| Recurso      | Método | Caminho                                   | Descrição                                   |
|--------------|--------|--------------------------------------------|----------------------------------------------|
| Livros       | POST   | `/api/v1/livros`                           | Cadastra um livro                             |
|              | POST   | `/api/v1/livros/lote`                      | Cadastra vários livros em lote                |
|              | GET    | `/api/v1/livros?titulo=&autor=&isbn=&page=`| Busca paginada com filtros                    |
|              | GET    | `/api/v1/livros/{id}`                      | Busca por id                                  |
|              | PUT    | `/api/v1/livros/{id}`                      | Atualiza                                      |
|              | DELETE | `/api/v1/livros/{id}`                      | Remove                                        |
| Usuários     | POST   | `/api/v1/usuarios`                         | Cadastra um usuário                           |
|              | GET    | `/api/v1/usuarios?nome=&page=`             | Busca paginada                                |
|              | GET    | `/api/v1/usuarios/{id}/emprestimos`        | Histórico de empréstimos                      |
|              | PUT    | `/api/v1/usuarios/{id}`                    | Atualiza                                      |
|              | DELETE | `/api/v1/usuarios/{id}`                    | Remove                                        |
| Empréstimos  | POST   | `/api/v1/emprestimos`                      | Registra empréstimo                           |
|              | PUT    | `/api/v1/emprestimos/{id}/devolucao`       | Registra devolução                            |
|              | GET    | `/api/v1/emprestimos?status=&usuarioId=`   | Lista paginada                                |
| Reservas     | POST   | `/api/v1/reservas`                         | Reserva um livro indisponível                 |
|              | PUT    | `/api/v1/reservas/{id}/cancelamento`       | Cancela reserva                               |
|              | GET    | `/api/v1/reservas?usuarioId=`              | Lista paginada                                |
| Relatórios   | GET    | `/api/v1/relatorios/livros-mais-emprestados` | Top 20 livros mais emprestados              |
|              | GET    | `/api/v1/relatorios/emprestimos-ativos`    | Empréstimos ativos + previsão de devolução    |

Exemplos completos de request/response estão disponíveis no Swagger UI.

## Licença

Projeto acadêmico — Tech Challenge Pós Tech FIAP.
