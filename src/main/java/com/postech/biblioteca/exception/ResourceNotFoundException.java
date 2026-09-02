package com.postech.biblioteca.exception;

/** Lançada quando um recurso solicitado (livro, usuário, empréstimo, reserva) não é encontrado. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String recurso, Object id) {
        return new ResourceNotFoundException(recurso + " não encontrado(a) com id: " + id);
    }
}
