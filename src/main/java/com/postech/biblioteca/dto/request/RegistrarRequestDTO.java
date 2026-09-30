package com.postech.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para autocadastro público de um novo usuário — sempre criado com
 * papel USUARIO (ver {@code AuthServiceImpl#registrar}). Para criar um
 * usuário com papel BIBLIOTECARIO, use {@code POST /api/v1/usuarios}
 * autenticado como um BIBLIOTECARIO existente.
 */
@Schema(description = "Dados para autocadastro de um novo usuário (sempre com papel USUARIO)")
public record RegistrarRequestDTO(

        @Schema(example = "Ana Souza")
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Schema(example = "ana.souza@example.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        String senha
) {
}
