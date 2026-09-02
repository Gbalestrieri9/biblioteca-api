package com.postech.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do Sistema de Biblioteca Online.
 *
 * <p>Tech Challenge — Fase 2 — Arquitetura e Desenvolvimento Java (Pós Tech).
 * API REST para cadastro de livros e usuários, controle de empréstimos e
 * devoluções, consulta com filtros e relatórios de uso da biblioteca.</p>
 */
@SpringBootApplication
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);
    }
}
