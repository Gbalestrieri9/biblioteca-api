# Collection Postman — Biblioteca Online API

Arquivo: `Biblioteca-Online-API.postman_collection.json`

## Como importar

1. Abra o Postman.
2. **File → Import** (ou arraste o arquivo para a janela do Postman).
3. Selecione `Biblioteca-Online-API.postman_collection.json`.

A collection já vem com a variável `baseUrl` configurada para
`http://localhost:8080` (padrão do `docker compose up`). Se a API estiver em
outra porta/host, edite a variável em **Collection → Variables**.

## Autenticação (Bearer Token)

A API exige autenticação JWT em quase todos os endpoints. A collection cuida
disso automaticamente:

* A pasta **"0. Autenticação"** faz login com o usuário BIBLIOTECARIO de
  demonstração (`admin@biblioteca.com` / `Admin@123`, criado pela migration
  `V3__add_autenticacao.sql`) e guarda o token retornado na variável de
  collection `accessToken`.
* Um script de **pre-request no nível da collection** injeta automaticamente
  o cabeçalho `Authorization: Bearer {{accessToken}}` em toda requisição
  seguinte — exceto nas rotas públicas (`/auth/login`, `/auth/registrar`) e
  nas duas requisições que já definem seu próprio cabeçalho `Authorization`
  (consultar `/auth/me` com o token da Ana e com o do Bruno).
* A mesma pasta também autocadastra dois usuários comuns (Ana e Bruno,
  papel `USUARIO`) via `POST /api/v1/auth/registrar` e guarda seus tokens
  (`tokenUsuarioAna`, `tokenUsuarioBruno`) e ids (`usuarioIdAna`,
  `usuarioIdBruno`, obtidos via `GET /api/v1/auth/me`), usados depois nos
  empréstimos e reservas.

**Importante:** a pasta 0 precisa rodar antes de qualquer outra, sempre que
a collection é executada — senão as demais requisições ficam sem token e
retornam 401, e as que dependem de `usuarioIdAna`/`usuarioIdBruno` (como
"Buscar Usuário por ID" ou os empréstimos) falham por falta desses ids.

## Dois fluxos de cadastro de usuário

* **`POST /api/v1/auth/registrar`** (público) — autocadastro, sempre com
  papel `USUARIO`. Usado na pasta 0 para Ana e Bruno.
* **`POST /api/v1/usuarios`** (BIBLIOTECARIO) — cadastro administrativo,
  quem cadastra escolhe nome/e-mail/senha **e o papel**. Usado na pasta 1
  para a Carla.

## Como rodar o fluxo completo

Com a API no ar (`docker compose up`):

1. Clique com o botão direito na collection **"Biblioteca Online API - Tech Challenge Fase 2"** e escolha **Run collection** (ou use o botão "Run" no topo).
2. Mantenha as pastas na ordem padrão (**0 a 5**) e clique em **Run Biblioteca Online API**.
3. Acompanhe os testes passando — cada request tem asserts automáticos (status HTTP, formato da resposta, regras de negócio, papel de acesso).

Não é necessário editar nada manualmente: o token de autenticação e os IDs
de livros, usuários, empréstimos e reservas criados em cada request são
capturados automaticamente em variáveis da collection (`accessToken`,
`livroId`, `usuarioIdAna`, `emprestimoId`, etc.) e reaproveitados pelas
próximas requisições.

## O que cada pasta cobre

| Pasta | Cobertura |
|-------|-----------|
| 0. Autenticação | Login do BIBLIOTECARIO de demonstração e autocadastro de dois usuários comuns (Ana e Bruno); preenche os tokens Bearer usados pelo resto da collection; consulta `/auth/me` |
| 1. Usuários | Cadastro administrativo (com senha/papel), busca por id, listagem com filtro, atualização (exigem papel BIBLIOTECARIO) |
| 2. Livros | Cadastro (individual e em lote), busca por id, busca paginada com filtro, atualização (cadastro/edição exigem papel BIBLIOTECARIO) |
| 3. Empréstimos | Empréstimo, consulta, listagem por status e devolução |
| 4. Reservas | Fluxo completo: livro com exemplar único fica indisponível após o empréstimo → reserva é criada → devolução libera o livro → cancelamento de reserva |
| 5. Relatórios | Top 20 livros mais emprestados e empréstimos ativos com previsão de devolução |

## Rodando de novo

Os e-mails de usuário e os ISBNs de livro usados na collection são **fixos**
(sem sufixo de timestamp), para deixar as requisições mais simples e
previsíveis. Isso tem um efeito colateral: rodar a collection inteira uma
segunda vez sem resetar o banco vai falhar nos cadastros que já existem —
usuários com `409`/`400` (e-mail já cadastrado) e livros com `409` (ISBN já
cadastrado).

Antes de rodar a collection de novo, resete o banco:

```bash
docker compose down -v
docker compose up --build
```

Dois dos livros cadastrados nesta collection ("Design Patterns", usado no
empréstimo da pasta 3, e "Working Effectively with Legacy Code", usado na
reserva da pasta 4) também nunca são excluídos pela própria collection — um
livro com histórico de empréstimo ou reserva não pode ser excluído, porque a
chave estrangeira de `emprestimos`/`reservas` para `livros` não tem
`ON DELETE CASCADE` (a tentativa falharia com `409 Conflito de dados`). Não
há problema em deixá-los no banco; o reset acima já cuida de limpar tudo.
