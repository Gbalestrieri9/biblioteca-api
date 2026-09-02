package com.postech.biblioteca.dto.response;

import java.time.Instant;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Instant dataCadastro
) {
}
