package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.response.RelatorioEmprestimoAtivoDTO;
import com.postech.biblioteca.dto.response.RelatorioLivroMaisEmprestadoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RelatorioService {

    /** Lista dos 20 livros mais emprestados da biblioteca. */
    List<RelatorioLivroMaisEmprestadoDTO> livrosMaisEmprestados();

    /** Livros emprestados no momento e a previsão de devolução, paginado. */
    Page<RelatorioEmprestimoAtivoDTO> emprestimosAtivos(Pageable pageable);
}
