package com.postech.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para registrar uma nova reserva")
public record ReservaRequestDTO(

        @Schema(description = "Identificador do livro a ser reservado", example = "1")
        @NotNull(message = "O identificador do livro é obrigatório")
        Long livroId,

        @Schema(description = "Identificador do usuário que está reservando o livro", example = "1")
        @NotNull(message = "O identificador do usuário é obrigatório")
        Long usuarioId
) {
}
