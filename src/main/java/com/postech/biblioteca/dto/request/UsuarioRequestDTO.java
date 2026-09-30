package com.postech.biblioteca.dto.request;

import com.postech.biblioteca.enums.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados para cadastro ou atualização administrativa de um usuário — endpoint
 * restrito a BIBLIOTECARIO (ver {@code UsuarioController}). Para um usuário
 * se autocadastrar (sempre com papel USUARIO), ver
 * {@code POST /api/v1/auth/registrar}.
 */
@Schema(description = "Dados para cadastro ou atualização administrativa de um usuário (endpoint restrito a BIBLIOTECARIO)")
public record UsuarioRequestDTO(

        @Schema(description = "Nome completo do usuário", example = "Ana Souza")
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Schema(description = "E-mail do usuário (único, usado para login)", example = "ana.souza@example.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(description = "Senha de acesso. Obrigatória ao cadastrar; na atualização, deixe em "
                + "branco para manter a senha atual", example = "Senha@123")
        String senha,

        @Schema(description = "Papel de acesso do usuário", example = "USUARIO")
        @NotNull(message = "O papel é obrigatório")
        Papel papel
) {
}
