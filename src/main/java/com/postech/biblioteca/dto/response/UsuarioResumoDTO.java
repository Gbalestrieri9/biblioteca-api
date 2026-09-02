package com.postech.biblioteca.dto.response;

/** Representação resumida de um usuário, usada dentro de outras respostas (empréstimo, reserva). */
public record UsuarioResumoDTO(
        Long id,
        String nome,
        String email
) {
}
