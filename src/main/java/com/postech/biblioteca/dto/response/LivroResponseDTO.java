package com.postech.biblioteca.dto.response;

import java.time.Instant;

public record LivroResponseDTO(
        Long id,
        String titulo,
        String autor,
        String isbn,
        Integer quantidadeTotal,
        Integer quantidadeDisponivel,
        boolean disponivel,
        Instant dataCadastro
) {
}
