package com.postech.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso emitido após login ou autocadastro")
public record TokenResponseDTO(

        @Schema(description = "Token JWT a ser enviado no cabeçalho 'Authorization: Bearer <token>' das demais requisições")
        String accessToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tipo,

        @Schema(description = "Tempo de expiração do token, em segundos, a partir do momento da emissão")
        long expiraEmSegundos
) {

    public static TokenResponseDTO of(String accessToken, long expiraEmSegundos) {
        return new TokenResponseDTO(accessToken, "Bearer", expiraEmSegundos);
    }
}
