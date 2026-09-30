package com.postech.biblioteca.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.biblioteca.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Resposta {@code 401} no mesmo formato padronizado de erro do resto da API
 * ({@link ErrorResponseDTO}) para requisições sem um token Bearer válido —
 * sem isso, o Spring Security responderia com um corpo vazio/diferente do
 * restante das respostas de erro tratadas por {@code GlobalExceptionHandler}
 * (que não intercepta exceções lançadas pela cadeia de filtros de
 * segurança, só as lançadas dentro dos controllers).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponseDTO body = ErrorResponseDTO.of(HttpStatus.UNAUTHORIZED.value(), "Não autenticado",
                "É necessário informar um token Bearer válido (ver POST /api/v1/auth/login)",
                request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
