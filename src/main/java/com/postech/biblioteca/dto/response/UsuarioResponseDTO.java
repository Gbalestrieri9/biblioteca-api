package com.postech.biblioteca.dto.response;

import com.postech.biblioteca.enums.Papel;

import java.time.Instant;

/** Nunca inclui a senha (nem o hash) — ver {@code Usuario#senha}. */
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Papel papel,
        Instant dataCadastro
) {
}
