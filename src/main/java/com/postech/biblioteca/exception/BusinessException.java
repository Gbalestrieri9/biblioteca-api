package com.postech.biblioteca.exception;

/**
 * Lançada quando uma regra de negócio impede a operação (ex.: livro sem
 * exemplar disponível, e-mail/ISBN duplicado, devolução de empréstimo já
 * concluído).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
