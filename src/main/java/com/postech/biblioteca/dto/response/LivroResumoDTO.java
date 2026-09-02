package com.postech.biblioteca.dto.response;

/** Representação resumida de um livro, usada dentro de outras respostas (empréstimo, reserva). */
public record LivroResumoDTO(
        Long id,
        String titulo,
        String autor,
        String isbn
) {
}
