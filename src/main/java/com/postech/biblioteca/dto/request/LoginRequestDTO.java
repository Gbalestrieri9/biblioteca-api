package com.postech.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequestDTO(

        @Schema(example = "admin@biblioteca.com")
        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @Schema(example = "Admin@123")
        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
