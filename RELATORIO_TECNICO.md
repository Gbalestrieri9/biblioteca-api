# Relatório Técnico — Sistema de Biblioteca Online

**Tech Challenge — Fase 2 (Substitutiva) — Arquitetura e Desenvolvimento Java — Pós Tech**

## 1. Objetivo

Este relatório descreve as tecnologias, decisões de arquitetura, desafios e
soluções adotadas no desenvolvimento do sistema de biblioteca online,
conforme os requisitos do desafio: CRUD de livros e usuários, controle de
empréstimos/devoluções, consulta com filtros, relatórios de uso, uso de
APIs modernas de Java, persistência eficiente, autenticação/autorização e
conteinerização com Docker.

## 2. Tecnologias e ferramentas utilizadas

* **Java 21** — versão LTS mais recente disponível no momento do desenvolvimento, usada para aproveitar `records` (DTOs imutáveis), `java.time` e streams.
* **Spring Boot 3.3** — framework principal, com os módulos:
  * `spring-boot-starter-web` — API REST;
  * `spring-boot-starter-data-jpa` — persistência via Hibernate;
  * `spring-boot-starter-validation` — Bean Validation (Jakarta Validation) nos DTOs de entrada;
  * `spring-boot-starter-security` — autenticação/autorização;
  * `spring-boot-starter-actuator` — health checks (usados pelo `HEALTHCHECK` do Docker);
* **jjwt (io.jsonwebtoken)** — geração e validação dos tokens JWT (assinatura HMAC-SHA256), usada por trás de `JwtService`.
* **PostgreSQL 16** — banco relacional escolhido pela maturidade, suporte a `TIMESTAMP WITH TIME ZONE` (essencial para a convenção de datas em UTC) e por ser o SGBD mais comum em ambientes corporativos Java.
* **Flyway** — versionamento e migração do schema, garantindo que a estrutura do banco seja reprodutível e auditável (`V1__create_tables.sql`, `V2__seed_data.sql`, `V3__add_autenticacao.sql`).
* **springdoc-openapi** — geração automática da documentação OpenAPI/Swagger a partir das anotações dos controllers e DTOs, disponível em `/swagger-ui.html`, com o esquema `bearerAuth` registrado globalmente (`OpenApiConfig`).
* **Lombok** — redução de boilerplate nas entidades (getters/setters/builders).
* **JUnit 5 + Mockito + AssertJ** — testes unitários da camada de serviço, isolando as regras de negócio do banco de dados.
* **Docker / Docker Compose** — conteinerização da aplicação e do banco de dados.

## 3. Decisões de arquitetura

### 3.1 Camadas e padrões de projeto

O projeto segue uma arquitetura em camadas clássica
(`controller → service → repository → banco`), com:

* **DTOs** (`dto.request` / `dto.response`) desacoplando o contrato público da API do modelo de persistência — mudanças na entidade JPA não quebram o contrato da API, e vice-versa.
* **Padrão Repository**, via Spring Data JPA, com **Specification** (`repository.specification`) para compor dinamicamente os filtros de busca de livros, usuários e empréstimos sem explodir em dezenas de métodos derivados (`findByTituloAndAutor`, `findByTituloAndIsbn`, ...).
* **Mapper** estático e sem estado (`mapper`) para conversão entidade ↔ DTO, mantendo a lógica de mapeamento em um único lugar e testável.
* **Builder** (via Lombok) nas entidades, favorecendo a criação de objetos imutáveis/consistentes em testes e serviços.
* **`@RestControllerAdvice`** centralizando o tratamento de erros (`GlobalExceptionHandler`), garantindo que toda a API retorne um formato de erro único e prevendo os principais cenários: recurso não encontrado (404), regra de negócio violada (422), conflito de integridade (409), erro de validação (400) e erro inesperado (500).
* **Injeção de dependência por construtor** (`@RequiredArgsConstructor`), favorecendo testabilidade (mocks) e imutabilidade dos componentes Spring.

### 3.2 APIs modernas de Java

* **`java.time`**: todo o cálculo de datas usa `Instant`, `LocalDate` e `ChronoUnit`, nunca `java.util.Date`/`Calendar`. O prazo de devolução é calculado com `LocalDate.now(ZONE_ID).plusDays(PRAZO_EMPRESTIMO_DIAS)`.
* **Streams**: usadas na camada de mapeamento (`list.stream().map(...).toList()`), na montagem dos DTOs de relatório e na composição de `Specification`s dinâmicas, evitando laços `for` imperativos e favorecendo processamento declarativo de coleções.
* **Records**: todos os DTOs são `record`s Java, imutáveis por padrão e com `equals`/`hashCode`/`toString` gerados automaticamente.

### 3.3 Convenção de data e hora (GMT/UTC)

Um dos pontos de atenção explícitos do desafio é deixar claro qual fuso
horário é utilizado. Neste projeto:

* Todo o sistema opera em **UTC (GMT+0)** — constante única em `AppConstants.ZONE_ID`.
* Colunas de data/hora no PostgreSQL são `TIMESTAMP WITH TIME ZONE`, e a coluna de data prevista de devolução é `DATE` (sem componente de hora).
* A serialização JSON usa o padrão ISO-8601 (`spring.jackson.time-zone=UTC`, `write-dates-as-timestamps=false`), então toda resposta da API traz datas no formato `2026-09-02T14:30:00Z` (instantes) ou `2026-09-16` (datas).
* Centralizar o fuso em uma única constante evita o clássico bug de "empréstimo vence um dia antes/depois" quando clientes de fusos diferentes consomem a API — a resposta é sempre não-ambígua.

### 3.4 Otimização de acesso a dados

* **Paginação obrigatória** em todos os endpoints de listagem (`Pageable`/`Page`), com tamanho máximo de página limitado (`AppConstants.PAGE_SIZE_MAXIMO = 100`) para evitar que um cliente solicite uma página excessivamente grande.
* **Índices** criados via Flyway nas colunas usadas pelos filtros (`titulo`, `autor`, `isbn`, `email`) e pelas consultas dos relatórios (`status`, `data_prevista_devolucao`, `livro_id + status`), evitando *full scans*.
* **Consultas agregadas no banco**: o relatório de livros mais emprestados usa `GROUP BY` + `COUNT` + `ORDER BY` diretamente em JPQL, com uma projeção (`LivroMaisEmprestadoProjection`) que traz apenas os campos necessários — em vez de carregar todos os empréstimos em memória para agregar em Java.
* **Operações em lote**: o endpoint `POST /api/v1/livros/lote` persiste múltiplos livros em uma única transação, aproveitando a configuração `hibernate.jdbc.batch_size=30` (com `order_inserts`/`order_updates` habilitados), que agrupa os `INSERT`s em lotes JDBC — reduzindo drasticamente o número de round-trips ao banco em cargas de dados maiores (ex.: importação inicial do acervo).
* **`open-in-view: false`**: desabilitado para evitar consultas *lazy* implícitas fora da camada de serviço/transação, forçando que todo o carregamento de dados aconteça de forma explícita e controlada.

### 3.5 Autenticação e autorização (JWT Bearer)

A API exige autenticação **stateless** via **JWT Bearer token** — nenhuma
sessão é guardada no servidor; o próprio token, assinado com HMAC-SHA256
(`JwtService`), carrega o e-mail e o papel do usuário a cada requisição,
permitindo escalar a API horizontalmente (`docker compose up --scale app=3`)
sem sticky sessions nem estado de sessão compartilhado entre réplicas.

Dois papéis de acesso (`Papel`: `BIBLIOTECARIO`/`USUARIO`) e as regras de
autorização ficam **centralizadas em `SecurityConfig`**, por rota + método
HTTP (`authorizeHttpRequests`), em vez de espalhadas em `@PreAuthorize`
pelos controllers — mantém os services focados em regra de negócio e as
regras de acesso visíveis e auditáveis em um único lugar: cadastro,
atualização e exclusão de livros/usuários exigem `BIBLIOTECARIO`; o
restante (consultas, empréstimos, devoluções, reservas, relatórios,
`/auth/me`) aceita qualquer usuário autenticado.

Peças principais:

* **`JwtAuthenticationFilter`** (`OncePerRequestFilter`) — extrai o Bearer token do cabeçalho `Authorization`, valida a assinatura/expiração e autentica a requisição no `SecurityContextHolder`; um token ausente/inválido/expirado simplesmente segue sem autenticar (a decisão de exigir ou não autenticação é do `SecurityConfig`, resultando em `401`/`403` quando aplicável).
* **`UsuarioDetailsServiceImpl`** — carrega o `Usuario` pelo e-mail e o expõe como `UserDetails`, com uma authority `ROLE_<papel>`.
* **`AuthController`/`AuthService`** — `POST /auth/login` (autentica via `AuthenticationManager` e emite o token), `POST /auth/registrar` (autocadastro público, sempre papel `USUARIO`) e `GET /auth/me` (dados do usuário autenticado).
* **`BCryptPasswordEncoder`** — único formato de senha persistido é o hash bcrypt (`Usuario#senha`); a senha em texto puro nunca é armazenada nem logada.

## 4. Desafios encontrados e soluções

* **Modelagem do status de empréstimo "atrasado":** inicialmente cogitamos um terceiro status persistido (`ATRASADO`), mas isso exigiria um job agendado rodando periodicamente só para atualizar o campo. Optamos por manter apenas `ATIVO`/`DEVOLVIDO` no banco e **calcular o atraso dinamicamente** (comparando `dataPrevistaDevolucao` com a data corrente) no momento da resposta — mais simples, sempre consistente, e sem necessidade de infraestrutura de agendamento.
* **Filtros combináveis de busca:** usar múltiplos métodos `findByXAndY` no Spring Data cresce de forma combinatória à medida que novos filtros são adicionados. A solução foi usar `JpaSpecificationExecutor` + `Specification`s pequenas e compostas (`LivroSpecification`, `EmprestimoSpecification`), que se combinam dinamicamente com `and()` apenas quando o filtro é informado.
* **Consistência da quantidade disponível de exemplares:** a quantidade de cópias disponíveis de um livro precisa ser decrementada/incrementada de forma consistente a cada empréstimo/devolução, sem ficar negativa nem exceder o total. Isso foi resolvido no `EmprestimoServiceImpl` dentro de uma transação (`@Transactional`), garantindo atomicidade entre a criação/atualização do empréstimo e a atualização da entidade `Livro`; a integridade adicional é reforçada por `CHECK` constraints no banco (`ck_livros_disponivel_menor_total`).
* **Retrofit de autenticação sobre um domínio já existente:** os usuários já cadastrados pelo seed (`V2__seed_data.sql`) não tinham senha/papel. A migration `V3__add_autenticacao.sql` adiciona as colunas como opcionais, popula um hash bcrypt padrão para os registros existentes (senha temporária, documentada no README) e só então aplica as restrições `NOT NULL`/`CHECK` — evitando quebrar dados já persistidos e mantendo o histórico de migrations do Flyway íntegro (nenhuma migration anterior foi editada).
* **Ambiente de desenvolvimento sem acesso à internet:** o ambiente usado para gerar este projeto não tinha acesso ao repositório Maven Central, o que impediu a compilação automatizada durante o desenvolvimento. Para mitigar o risco, o código foi escrito seguindo estritamente as assinaturas e convenções conhecidas do Spring Boot 3.3/Spring Security 6.x/JJWT 0.12.x, com revisão manual cuidadosa de cada classe. **Recomenda-se rodar `mvn clean test` como primeiro passo ao clonar o repositório**, antes de seguir para o Docker.

## 5. Uso do Docker para escalabilidade

A aplicação é conteinerizada com um **Dockerfile multi-stage**:

1. **Estágio de build** (`maven:3.9-eclipse-temurin-21`): compila o projeto e gera o `.jar`; este estágio não vai para a imagem final.
2. **Estágio de runtime** (`eclipse-temurin:21-jre-alpine`): imagem enxuta contendo apenas o JRE e o `.jar` gerado, rodando com usuário não-root e com `HEALTHCHECK` configurado (consultando `/actuator/health`).

O `docker-compose.yml` orquestra dois serviços — `app` (API) e `db`
(PostgreSQL) — comunicando-se por uma rede interna do Compose, com o banco
persistindo dados em um *volume* nomeado (`biblioteca-db-data`), e o serviço
`app` só sobe depois que o `db` está saudável (`depends_on` +
`healthcheck`).

**Caminho de escalabilidade futura:**

* A aplicação é **stateless** (todo o estado vive no PostgreSQL, e a própria autenticação é stateless via JWT — nenhuma sessão fica guardada no processo da aplicação), então pode ser escalada horizontalmente apenas subindo mais réplicas do serviço `app` atrás de um load balancer (`docker compose up --scale app=3` + um proxy reverso como Nginx/Traefik na frente, ou um Ingress em Kubernetes) — sem precisar de sticky sessions.
* O uso de **paginação** e **operações em lote** já prepara o sistema para volumes maiores de dados sem degradar o tempo de resposta.
* Para produção, o próximo passo natural seria: (1) mover a imagem para um registry (Docker Hub/ECR/GCR), (2) orquestrar com Kubernetes ou um serviço gerenciado (ECS, Cloud Run), (3) adicionar um *connection pool* dimensionado por réplica e um banco gerenciado com réplicas de leitura, (4) introduzir cache (ex.: Redis) para os relatórios, que são consultas mais pesadas e podem ser servidas com uma janela de atualização de alguns minutos, e (5) mover o `JWT_SECRET` para um gerenciador de segredos (Vault, AWS Secrets Manager, etc.) em vez de uma variável de ambiente simples.

## 6. Considerações finais

O sistema atende às funcionalidades exigidas (CRUD de livros, CRUD de
usuários, empréstimo/devolução com cálculo de prazo, consulta com filtros e
paginação, reservas e os dois relatórios solicitados), protege a API com
autenticação/autorização JWT Bearer stateless por papel, aproveita APIs
modernas do Java (`java.time`, Streams, `records`), documenta a API via
Swagger/OpenAPI (com o esquema `bearerAuth` pronto para uso) e Javadoc, e
está pronto para ser executado em qualquer ambiente via Docker Compose.
