package com.postech.biblioteca.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.biblioteca.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Resposta {@code 403} no mesmo formato padronizado de erro do resto da API
 * ({@link ErrorResponseDTO}), usada quando o usuário está autenticado mas
 * não tem o papel exigido pela rota (ver regras em {@code SecurityConfig}).
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponseDTO body = ErrorResponseDTO.of(HttpStatus.FORBIDDEN.value(), "Acesso negado",
                "Seu usuário não tem papel suficiente para acessar este recurso",
                request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
