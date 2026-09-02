package com.postech.biblioteca.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope de paginação exposto pela API, desacoplado de {@code
 * org.springframework.data.domain.Page} (evita vazar detalhes internos do
 * Spring Data no contrato público da API e mantém o JSON estável).
 */
public record PageResponseDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <E, D> PageResponseDTO<D> from(Page<E> page, Function<E, D> mapper) {
        return new PageResponseDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
