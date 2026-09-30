#!/usr/bin/env python3
"""
Gera a collection Postman (v2.1) da API do Sistema de Biblioteca Online.

Versão simplificada: só as chamadas "originais" do fluxo de uso da API
(sem requisições de exemplo que só existem para provar regra de negócio
— ex.: repetir uma ação para ver o erro) e sem {{$timestamp}} nos e-mails
de teste (e-mails fixos). Isso implica que, para rodar a collection uma
segunda vez sem conflito de e-mail/ISBN duplicado, o banco precisa ser
resetado entre execuções (ver postman/README.md).

Rode com: python3 generate_collection.py
Gera: Biblioteca-Online-API.postman_collection.json
"""

import json
import uuid


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def test_event(lines):
    return {"listen": "test", "script": {"type": "text/javascript", "exec": lines}}


def make_url(path, query=None):
    raw = "{{baseUrl}}" + path
    parts = [p for p in path.split("/") if p]
    url = {"raw": raw, "host": ["{{baseUrl}}"], "path": parts}
    if query:
        qs = "&".join(f"{k}={v}" for k, v in query)
        url["raw"] = raw + "?" + qs
        url["query"] = [{"key": k, "value": v} for k, v in query]
    return url


def make_body(raw_json_str):
    return {"mode": "raw", "raw": raw_json_str, "options": {"raw": {"language": "json"}}}


def make_request(name, method, path, *, query=None, headers=None, body=None,
                  description=None, test_lines=None):
    item = {"name": name}
    if test_lines:
        item["event"] = [test_event(test_lines)]
    req = {"method": method, "header": headers or []}
    if body is not None:
        req["body"] = make_body(body)
    req["url"] = make_url(path, query)
    if description:
        req["description"] = description
    item["request"] = req
    item["response"] = []
    return item


def make_folder(name, description, items):
    return {"name": name, "item": items, "description": description}


# ---------------------------------------------------------------------------
# 0. Autenticação
# ---------------------------------------------------------------------------

autenticacao = make_folder(
    "0. Autenticação",
    "Login do BIBLIOTECARIO de demonstração e autocadastro de dois usuários "
    "comuns (Ana e Bruno); preenche os tokens Bearer usados pelo resto da "
    "collection; consulta /auth/me.",
    [
        make_request(
            "Login - Bibliotecário (usuário de demonstração)", "POST", "/api/v1/auth/login",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "email": "admin@biblioteca.com",\n  "senha": "Admin@123"\n}',
            description=(
                "Autentica com o usuário BIBLIOTECARIO criado pela migration "
                "V3__add_autenticacao.sql (login/senha de demonstração — troque "
                "em qualquer ambiente real). O token vira o padrão "
                "({{accessToken}}) usado por toda a collection, já que a "
                "maioria dos endpoints aceita qualquer papel autenticado e os "
                "de gestão de acervo/usuários exigem justamente BIBLIOTECARIO."
            ),
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Retornou um token Bearer', function () {",
                "    pm.expect(jsonData.accessToken).to.be.a('string').that.is.not.empty;",
                "    pm.expect(jsonData.tipo).to.eql('Bearer');",
                "});",
                "pm.collectionVariables.set('accessToken', jsonData.accessToken);",
                "pm.collectionVariables.set('tokenBibliotecario', jsonData.accessToken);",
            ],
        ),
        make_request(
            "Autocadastro - Ana (papel USUARIO)", "POST", "/api/v1/auth/registrar",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "nome": "Ana Souza",\n  "email": "ana.souza@example.com",\n  "senha": "Senha@123"\n}',
            description=(
                "Autocadastro público — sempre cria o usuário com papel "
                "USUARIO (ver 'POST /api/v1/usuarios' para criar com papel "
                "BIBLIOTECARIO)."
            ),
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Retornou um token Bearer', function () {",
                "    pm.expect(jsonData.accessToken).to.be.a('string').that.is.not.empty;",
                "});",
                "pm.collectionVariables.set('tokenUsuarioAna', jsonData.accessToken);",
            ],
        ),
        make_request(
            "Consultar Perfil - Ana (/auth/me)", "GET", "/api/v1/auth/me",
            headers=[{"key": "Authorization", "value": "Bearer {{tokenUsuarioAna}}"}],
            description=(
                "Confirma os dados do usuário a partir do próprio token (sem "
                "precisar saber o id de antemão) e captura o id da Ana para "
                "uso posterior (empréstimos, reservas)."
            ),
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Papel é USUARIO', function () {",
                "    pm.expect(jsonData.papel).to.eql('USUARIO');",
                "});",
                "pm.collectionVariables.set('usuarioIdAna', jsonData.id);",
            ],
        ),
        make_request(
            "Autocadastro - Bruno (papel USUARIO)", "POST", "/api/v1/auth/registrar",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "nome": "Bruno Lima",\n  "email": "bruno.lima@example.com",\n  "senha": "Senha@123"\n}',
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.collectionVariables.set('tokenUsuarioBruno', jsonData.accessToken);",
            ],
        ),
        make_request(
            "Consultar Perfil - Bruno (/auth/me)", "GET", "/api/v1/auth/me",
            headers=[{"key": "Authorization", "value": "Bearer {{tokenUsuarioBruno}}"}],
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.collectionVariables.set('usuarioIdBruno', jsonData.id);",
            ],
        ),
    ],
)

# ---------------------------------------------------------------------------
# 1. Usuários
# ---------------------------------------------------------------------------

usuarios = make_folder(
    "1. Usuários",
    "Cadastro administrativo (com senha/papel), busca por id, listagem com "
    "filtro e atualização (exigem papel BIBLIOTECARIO).",
    [
        make_request(
            "Cadastrar Usuário Administrativamente - Carla", "POST", "/api/v1/usuarios",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "nome": "Carla Mendes",\n  "email": "carla.mendes@example.com",\n  "senha": "Senha@123",\n  "papel": "USUARIO"\n}',
            description=(
                "Cadastro administrativo (BIBLIOTECARIO) — diferente do "
                "autocadastro ('POST /api/v1/auth/registrar'), aqui quem "
                "cadastra escolhe o papel do usuário (também poderia ser "
                "BIBLIOTECARIO)."
            ),
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Usuário criado com papel USUARIO', function () {",
                "    pm.expect(jsonData.papel).to.eql('USUARIO');",
                "});",
                "pm.collectionVariables.set('usuarioIdCarla', jsonData.id);",
            ],
        ),
        make_request(
            "Buscar Usuário por ID", "GET", "/api/v1/usuarios/{{usuarioIdAna}}",
            description="Consulta simples aceita qualquer papel autenticado.",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Usuário possui os campos esperados (sem senha)', function () {",
                "    const jsonData = pm.response.json();",
                "    pm.expect(jsonData.nome).to.eql('Ana Souza');",
                "    pm.expect(jsonData).to.not.have.property('senha');",
                "});",
            ],
        ),
        make_request(
            "Listar Usuários (filtro por nome)", "GET", "/api/v1/usuarios",
            query=[("nome", "Carla"), ("page", "0"), ("size", "10")],
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Encontrou ao menos 1 resultado', function () {",
                "    pm.expect(pm.response.json().content.length).to.be.above(0);",
                "});",
            ],
        ),
        make_request(
            "Atualizar Usuário (mantendo a senha atual)", "PUT", "/api/v1/usuarios/{{usuarioIdCarla}}",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "nome": "Carla Mendes Silva",\n  "email": "carla.mendes@example.com",\n  "papel": "USUARIO"\n}',
            description=(
                "Atualiza nome/e-mail/papel sem informar 'senha' — o campo é "
                "opcional na atualização e, em branco/ausente, mantém o hash "
                "atual (ver UsuarioMapper.atualizarEntity). Requer "
                "BIBLIOTECARIO."
            ),
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Nome foi atualizado', function () {",
                "    pm.expect(pm.response.json().nome).to.eql('Carla Mendes Silva');",
                "});",
            ],
        ),
        make_request(
            "Excluir Usuário (Carla)", "DELETE", "/api/v1/usuarios/{{usuarioIdCarla}}",
            description=(
                "Atenção: só é possível excluir um usuário sem "
                "empréstimos/reservas vinculados no banco (FK). Rode por "
                "último."
            ),
            test_lines=[
                "pm.test('Status 204 No Content ou 409 se houver vínculo', function () {",
                "    pm.expect([204, 409]).to.include(pm.response.code);",
                "});",
            ],
        ),
    ],
)

# ---------------------------------------------------------------------------
# 2. Livros
# ---------------------------------------------------------------------------

livros = make_folder(
    "2. Livros",
    "Cadastro (individual e em lote), busca por id, busca paginada com "
    "filtro e atualização (cadastro/edição exigem papel BIBLIOTECARIO).",
    [
        make_request(
            "Cadastrar Livro - Design Patterns", "POST", "/api/v1/livros",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "titulo": "Design Patterns: Elements of Reusable Object-Oriented Software",\n  "autor": "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides",\n  "isbn": "9780201633610",\n  "quantidadeTotal": 3\n}',
            description=(
                "Cadastro administrativo (requer BIBLIOTECARIO). Este livro é "
                "usado no fluxo de empréstimo da pasta 3."
            ),
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Livro criado com os dados enviados', function () {",
                "    pm.expect(jsonData.titulo).to.include('Design Patterns');",
                "    pm.expect(jsonData.quantidadeDisponivel).to.eql(3);",
                "});",
                "pm.collectionVariables.set('livroId', jsonData.id);",
            ],
        ),
        make_request(
            "Cadastrar Livro - The Pragmatic Programmer", "POST", "/api/v1/livros",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "titulo": "The Pragmatic Programmer: Your Journey to Mastery",\n  "autor": "David Thomas, Andrew Hunt",\n  "isbn": "9780135957059",\n  "quantidadeTotal": 2\n}',
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.collectionVariables.set('livroId2', jsonData.id);",
            ],
        ),
        make_request(
            "Cadastrar Livros em Lote", "POST", "/api/v1/livros/lote",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='[\n  {\n    "titulo": "Test Driven Development: By Example",\n    "autor": "Kent Beck",\n    "isbn": "9780321146533",\n    "quantidadeTotal": 2\n  },\n  {\n    "titulo": "Continuous Delivery",\n    "autor": "Jez Humble, David Farley",\n    "isbn": "9780321601919",\n    "quantidadeTotal": 1\n  }\n]',
            description=(
                "Demonstra o cadastro em lote (requisito de 'operações em "
                "lote'). Retorna um array com os livros criados."
            ),
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Retornou 2 livros criados', function () {",
                "    pm.expect(jsonData).to.have.lengthOf(2);",
                "});",
                "pm.collectionVariables.set('livroIdLote1', jsonData[0].id);",
                "pm.collectionVariables.set('livroIdLote2', jsonData[1].id);",
            ],
        ),
        make_request(
            "Buscar Livro por ID", "GET", "/api/v1/livros/{{livroId}}",
            description="Consulta simples; qualquer papel autenticado pode acessar.",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Livro possui os campos esperados', function () {",
                "    const jsonData = pm.response.json();",
                "    pm.expect(jsonData.titulo).to.include('Design Patterns');",
                "    pm.expect(jsonData).to.have.property('disponivel', true);",
                "});",
            ],
        ),
        make_request(
            "Listar Livros (filtro por título)", "GET", "/api/v1/livros",
            query=[("titulo", "Pragmatic"), ("page", "0"), ("size", "10")],
            description=(
                "Busca paginada com filtro por título (case-insensitive, "
                "'contém'). Qualquer papel autenticado pode consultar."
            ),
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Encontrou ao menos 1 resultado', function () {",
                "    pm.expect(pm.response.json().content.length).to.be.above(0);",
                "});",
            ],
        ),
        make_request(
            "Listar Livros (paginado, sem filtro)", "GET", "/api/v1/livros",
            query=[("page", "0"), ("size", "5"), ("sort", "titulo"), ("direction", "asc")],
            description="Lista o acervo paginado (tamanho de página 5), sem filtros.",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Resposta contém metadados de paginação', function () {",
                "    const jsonData = pm.response.json();",
                "    pm.expect(jsonData).to.have.all.keys('content', 'pageNumber', 'pageSize', 'totalElements', 'totalPages', 'first', 'last');",
                "});",
            ],
        ),
        make_request(
            "Atualizar Livro", "PUT", "/api/v1/livros/{{livroId2}}",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "titulo": "The Pragmatic Programmer (20th Anniversary Edition)",\n  "autor": "David Thomas, Andrew Hunt",\n  "isbn": "9780135957059",\n  "quantidadeTotal": 4\n}',
            description="Atualiza o título e a quantidade total do livro cadastrado (BIBLIOTECARIO).",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Título foi atualizado', function () {",
                "    pm.expect(pm.response.json().titulo).to.include('20th Anniversary');",
                "});",
            ],
        ),
        make_request(
            "Excluir Livro (The Pragmatic Programmer)", "DELETE", "/api/v1/livros/{{livroId2}}",
            description="Nunca foi emprestado, então pode ser removido com segurança.",
            test_lines=[
                "pm.test('Status 204 No Content', function () {",
                "    pm.response.to.have.status(204);",
                "});",
            ],
        ),
    ],
)

# ---------------------------------------------------------------------------
# 3. Empréstimos
# ---------------------------------------------------------------------------

emprestimos = make_folder(
    "3. Empréstimos",
    "Empréstimo, consulta, listagem por status e devolução.",
    [
        make_request(
            "Emprestar Livro - Ana pega Design Patterns", "POST", "/api/v1/emprestimos",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "livroId": "{{livroId}}",\n  "usuarioId": "{{usuarioIdAna}}"\n}',
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Empréstimo criado com status ATIVO e prazo calculado', function () {",
                "    pm.expect(jsonData.status).to.eql('ATIVO');",
                "    pm.expect(jsonData.dataPrevistaDevolucao).to.be.a('string').that.is.not.empty;",
                "});",
                "pm.collectionVariables.set('emprestimoId', jsonData.id);",
            ],
        ),
        make_request(
            "Buscar Empréstimo por ID", "GET", "/api/v1/emprestimos/{{emprestimoId}}",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
            ],
        ),
        make_request(
            "Listar Empréstimos (filtro por status ATIVO)", "GET", "/api/v1/emprestimos",
            query=[("status", "ATIVO"), ("page", "0"), ("size", "10")],
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Encontrou ao menos 1 resultado', function () {",
                "    pm.expect(pm.response.json().content.length).to.be.above(0);",
                "});",
            ],
        ),
        make_request(
            "Devolver Empréstimo", "PUT", "/api/v1/emprestimos/{{emprestimoId}}/devolucao",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Status virou DEVOLVIDO', function () {",
                "    pm.expect(pm.response.json().status).to.eql('DEVOLVIDO');",
                "});",
            ],
        ),
    ],
)

# ---------------------------------------------------------------------------
# 4. Reservas
# ---------------------------------------------------------------------------

reservas = make_folder(
    "4. Reservas",
    "Fluxo completo: livro com exemplar único fica indisponível após o "
    "empréstimo → reserva é criada → devolução libera o livro → "
    "cancelamento de reserva.",
    [
        make_request(
            "Cadastrar Livro com Exemplar Único - Working Effectively with Legacy Code",
            "POST", "/api/v1/livros",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "titulo": "Working Effectively with Legacy Code",\n  "autor": "Michael Feathers",\n  "isbn": "9780131177055",\n  "quantidadeTotal": 1\n}',
            description=(
                "Só 1 exemplar — depois de emprestado, fica indisponível, "
                "montando o cenário para a reserva."
            ),
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.collectionVariables.set('livroUnicoId', jsonData.id);",
            ],
        ),
        make_request(
            "Emprestar o Exemplar Único para o Bruno", "POST", "/api/v1/emprestimos",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "livroId": "{{livroUnicoId}}",\n  "usuarioId": "{{usuarioIdBruno}}"\n}',
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "pm.collectionVariables.set('emprestimoUnicoId', pm.response.json().id);",
            ],
        ),
        make_request(
            "Reservar o Livro Indisponível para a Ana", "POST", "/api/v1/reservas",
            headers=[{"key": "Content-Type", "value": "application/json"}],
            body='{\n  "livroId": "{{livroUnicoId}}",\n  "usuarioId": "{{usuarioIdAna}}"\n}',
            test_lines=[
                "pm.test('Status 201 Created', function () {",
                "    pm.response.to.have.status(201);",
                "});",
                "const jsonData = pm.response.json();",
                "pm.test('Reserva criada com status ATIVA', function () {",
                "    pm.expect(jsonData.status).to.eql('ATIVA');",
                "});",
                "pm.collectionVariables.set('reservaId', jsonData.id);",
            ],
        ),
        make_request(
            "Listar Reservas (filtro por usuário)", "GET", "/api/v1/reservas",
            query=[("usuarioId", "{{usuarioIdAna}}"), ("page", "0"), ("size", "10")],
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Encontrou a reserva criada', function () {",
                "    pm.expect(pm.response.json().content.length).to.be.above(0);",
                "});",
            ],
        ),
        make_request(
            "Devolver o Exemplar Único (libera o livro)", "PUT",
            "/api/v1/emprestimos/{{emprestimoUnicoId}}/devolucao",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
            ],
        ),
        make_request(
            "Cancelar a Reserva", "PUT", "/api/v1/reservas/{{reservaId}}/cancelamento",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Status virou CANCELADA', function () {",
                "    pm.expect(pm.response.json().status).to.eql('CANCELADA');",
                "});",
            ],
        ),
    ],
)

# ---------------------------------------------------------------------------
# 5. Relatórios
# ---------------------------------------------------------------------------

relatorios = make_folder(
    "5. Relatórios",
    "Top 20 livros mais emprestados e empréstimos ativos com previsão de "
    "devolução.",
    [
        make_request(
            "Top 20 Livros Mais Emprestados", "GET", "/api/v1/relatorios/livros-mais-emprestados",
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Retornou uma lista', function () {",
                "    pm.expect(pm.response.json()).to.be.an('array');",
                "});",
            ],
        ),
        make_request(
            "Empréstimos Ativos com Previsão de Devolução", "GET",
            "/api/v1/relatorios/emprestimos-ativos",
            query=[("page", "0"), ("size", "10")],
            test_lines=[
                "pm.test('Status 200 OK', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "pm.test('Resposta é paginada', function () {",
                "    pm.expect(pm.response.json()).to.have.property('content');",
                "});",
            ],
        ),
    ],
)

# ---------------------------------------------------------------------------
# Collection
# ---------------------------------------------------------------------------

collection = {
    "info": {
        "_postman_id": str(uuid.uuid4()),
        "name": "Biblioteca Online API - Tech Challenge Fase 2",
        "description": (
            "Collection de testes end-to-end da API REST do Sistema de "
            "Biblioteca Online (Tech Challenge Fase 2 - Arquitetura e "
            "Desenvolvimento Java, Pós Tech), incluindo autenticação JWT "
            "Bearer.\n\n"
            "Rode as pastas em ordem (0 a 5) usando o Postman Runner (botão "
            "'Run' na collection) para exercitar o fluxo completo: "
            "login/registro, cadastro de livros e usuários, "
            "empréstimo/devolução, reservas e relatórios.\n\n"
            "Um script de pre-request no nível da collection injeta "
            "automaticamente 'Authorization: Bearer {{accessToken}}' em toda "
            "requisição (a pasta 0 preenche essa variável ao logar como o "
            "BIBLIOTECARIO de demonstração) — não é necessário copiar/colar "
            "tokens manualmente. Todas as demais variáveis (livroId, "
            "usuarioId, emprestimoId, etc.) também são preenchidas "
            "automaticamente pelos scripts de teste de cada request.\n\n"
            "Pré-requisito: a API rodando em {{baseUrl}} (padrão: "
            "http://localhost:8080, ver docker-compose.yml) com as "
            "migrations do Flyway aplicadas (o usuário BIBLIOTECARIO de "
            "demonstração é criado por V3__add_autenticacao.sql).\n\n"
            "Os e-mails e ISBNs de teste são fixos: rodar a collection uma "
            "segunda vez sem resetar o banco "
            "('docker compose down -v && docker compose up --build') fará "
            "os cadastros falharem com 409/400 (e-mail ou ISBN já "
            "existente)."
        ),
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "item": [autenticacao, usuarios, livros, emprestimos, reservas, relatorios],
    "event": [
        {
            "listen": "prerequest",
            "script": {
                "type": "text/javascript",
                "exec": [
                    "// Injeta automaticamente 'Authorization: Bearer {{accessToken}}' em",
                    "// toda requisição, exceto nas rotas públicas de autenticação e nas",
                    "// requisições que já definem seu próprio cabeçalho Authorization",
                    "// (usadas para consultar /auth/me com o token da Ana ou do Bruno).",
                    "const rotasPublicas = ['/api/v1/auth/login', '/api/v1/auth/registrar'];",
                    "const url = pm.request.url.toString();",
                    "const ehRotaPublica = rotasPublicas.some((rota) => url.includes(rota));",
                    "const jaTemAuthorization = pm.request.headers.has('Authorization');",
                    "",
                    "if (!ehRotaPublica && !jaTemAuthorization) {",
                    "    pm.request.headers.upsert({",
                    "        key: 'Authorization',",
                    "        value: 'Bearer ' + pm.collectionVariables.get('accessToken')",
                    "    });",
                    "}",
                ],
            },
        }
    ],
    "variable": [
        {"key": "baseUrl", "value": "http://localhost:8080"},
        {"key": "accessToken", "value": ""},
        {"key": "tokenBibliotecario", "value": ""},
        {"key": "tokenUsuarioAna", "value": ""},
        {"key": "tokenUsuarioBruno", "value": ""},
        {"key": "usuarioIdAna", "value": ""},
        {"key": "usuarioIdBruno", "value": ""},
        {"key": "usuarioIdCarla", "value": ""},
        {"key": "livroId", "value": ""},
        {"key": "livroId2", "value": ""},
        {"key": "livroIdLote1", "value": ""},
        {"key": "livroIdLote2", "value": ""},
        {"key": "livroUnicoId", "value": ""},
        {"key": "emprestimoId", "value": ""},
        {"key": "emprestimoUnicoId", "value": ""},
        {"key": "reservaId", "value": ""},
    ],
}

if __name__ == "__main__":
    out_path = "Biblioteca-Online-API.postman_collection.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(collection, f, indent=2, ensure_ascii=False)
        f.write("\n")

    total_requests = sum(len(folder["item"]) for folder in collection["item"])
    print(f"Gerado: {out_path}")
    print(f"Pastas: {len(collection['item'])}")
    print(f"Requisições: {total_requests}")
    for folder in collection["item"]:
        print(f"  - {folder['name']}: {len(folder['item'])} requisições")
