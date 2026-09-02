package com.postech.biblioteca.config;

import java.time.ZoneOffset;
import java.time.ZoneId;

/**
 * Constantes globais da aplicação.
 *
 * <p><b>Padrão de data/hora:</b> todo instante ({@code Instant} /
 * {@code OffsetDateTime}) é persistido e serializado em <b>UTC (GMT+0)</b>,
 * seguindo o formato ISO-8601 (ex.: {@code 2026-09-02T14:30:00Z}). Datas sem
 * componente de hora (como o prazo de devolução) usam {@code LocalDate} e são
 * calculadas a partir do relógio em UTC. Definir um único fuso para todo o
 * sistema evita ambiguidade no cálculo dos prazos de empréstimo,
 * independentemente de onde o cliente da API esteja localizado.</p>
 */
public final class AppConstants {

    private AppConstants() {
    }

    /** Fuso horário oficial utilizado por toda a aplicação: UTC (GMT+0). */
    public static final ZoneId ZONE_ID = ZoneOffset.UTC;

    /** Prazo padrão, em dias corridos, para devolução de um livro emprestado. */
    public static final int PRAZO_EMPRESTIMO_DIAS = 14;

    /** Tamanho de página padrão usado nos endpoints paginados. */
    public static final int PAGE_SIZE_PADRAO = 20;

    /** Tamanho máximo de página aceito pelos endpoints paginados. */
    public static final int PAGE_SIZE_MAXIMO = 100;

    /** Quantidade de itens no relatório de livros mais emprestados. */
    public static final int TOP_LIVROS_MAIS_EMPRESTADOS = 20;
}
