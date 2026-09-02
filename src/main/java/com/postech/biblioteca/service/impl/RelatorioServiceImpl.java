package com.postech.biblioteca.service.impl;

import com.postech.biblioteca.config.AppConstants;
import com.postech.biblioteca.dto.response.RelatorioEmprestimoAtivoDTO;
import com.postech.biblioteca.dto.response.RelatorioLivroMaisEmprestadoDTO;
import com.postech.biblioteca.enums.StatusEmprestimo;
import com.postech.biblioteca.mapper.EmprestimoMapper;
import com.postech.biblioteca.repository.EmprestimoRepository;
import com.postech.biblioteca.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Geração dos relatórios de uso da biblioteca, usando a API de Streams para
 * transformar as projeções/entidades vindas do banco nos DTOs de resposta.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioServiceImpl implements RelatorioService {

    private final EmprestimoRepository emprestimoRepository;

    @Override
    public List<RelatorioLivroMaisEmprestadoDTO> livrosMaisEmprestados() {
        Pageable top20 = PageRequest.of(0, AppConstants.TOP_LIVROS_MAIS_EMPRESTADOS);

        return emprestimoRepository.buscarLivrosMaisEmprestados(top20).stream()
                .map(p -> new RelatorioLivroMaisEmprestadoDTO(
                        p.getLivroId(), p.getTitulo(), p.getAutor(), p.getIsbn(), p.getTotalEmprestimos()))
                .toList();
    }

    @Override
    public Page<RelatorioEmprestimoAtivoDTO> emprestimosAtivos(Pageable pageable) {
        return emprestimoRepository.findByStatus(StatusEmprestimo.ATIVO, pageable)
                .map(EmprestimoMapper::toRelatorioAtivoDTO);
    }
}
