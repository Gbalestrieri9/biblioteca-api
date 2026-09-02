package com.postech.biblioteca.config;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Constrói objetos {@link Pageable} a partir dos parâmetros de query dos
 * controllers, aplicando limites de tamanho de página (requisito de
 * "otimização de acesso a dados": evita que um cliente solicite páginas
 * enormes e sobrecarregue o banco/rede).
 */
public final class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pageable of(int page, int size, String sortBy, String direction) {
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), AppConstants.PAGE_SIZE_MAXIMO);

        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = (sortBy == null || sortBy.isBlank()) ? Sort.unsorted() : Sort.by(dir, sortBy);

        return PageRequest.of(paginaSegura, tamanhoSeguro, sort);
    }
}
