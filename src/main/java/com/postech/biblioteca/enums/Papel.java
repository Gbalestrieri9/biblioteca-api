package com.postech.biblioteca.enums;

/**
 * Papel (perfil de acesso) de um usuário autenticado na API.
 *
 * <p>{@code BIBLIOTECARIO} tem acesso aos endpoints administrativos
 * (cadastro/atualização/exclusão de livros e usuários); {@code USUARIO} tem
 * acesso ao restante da API (consultas, empréstimos, reservas, relatórios).
 * Ver {@code SecurityConfig} para as regras de autorização por papel.</p>
 */
public enum Papel {
    BIBLIOTECARIO,
    USUARIO
}
