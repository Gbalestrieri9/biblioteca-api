package com.postech.biblioteca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentação OpenAPI/Swagger, disponível em runtime em
 * {@code /swagger-ui.html} e {@code /v3/api-docs}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Biblioteca Online API",
                version = "1.0.0",
                description = """
                        API REST do Sistema de Biblioteca Online — Tech Challenge Fase 2
                        (Arquitetura e Desenvolvimento Java, Pós Tech).

                        Permite cadastrar livros e usuários, registrar empréstimos e
                        devoluções, consultar o acervo com filtros e paginação, gerenciar
                        reservas e gerar relatórios de uso da biblioteca.

                        Todas as datas/horas são representadas em UTC (GMT+0), formato
                        ISO-8601 (ex.: 2026-09-02T14:30:00Z). Datas sem horário (prazo de
                        devolução) usam o formato ISO-8601 de data (yyyy-MM-dd).
                        """,
                contact = @Contact(name = "Tech Challenge - Pós Tech", url = "https://github.com"),
                license = @License(name = "MIT")
        )
)
public class OpenApiConfig {
}
