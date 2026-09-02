package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.LivroRequestDTO;
import com.postech.biblioteca.dto.response.LivroResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LivroService {

    LivroResponseDTO cadastrar(LivroRequestDTO dto);

    /** Cadastro em lote — otimiza a inserção de múltiplos livros em uma única transação/lote JDBC. */
    List<LivroResponseDTO> cadastrarEmLote(List<LivroRequestDTO> dtos);

    LivroResponseDTO buscarPorId(Long id);

    Page<LivroResponseDTO> buscar(String titulo, String autor, String isbn, Boolean disponivel, Pageable pageable);

    LivroResponseDTO atualizar(Long id, LivroRequestDTO dto);

    void excluir(Long id);
}
