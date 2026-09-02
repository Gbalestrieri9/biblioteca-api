package com.postech.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para cadastro ou atualização de um usuário")
public record UsuarioRequestDTO(

        @Schema(description = "Nome completo do usuário", example = "Ana Souza")
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Schema(description = "E-mail do usuário (único)", example = "ana.souza@example.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email
) {
}
