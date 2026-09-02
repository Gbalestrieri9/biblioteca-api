package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.UsuarioRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto);

    UsuarioResponseDTO buscarPorId(Long id);

    Page<UsuarioResponseDTO> listar(String nome, Pageable pageable);

    UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto);

    void excluir(Long id);

    /** Histórico de empréstimos (ativos e finalizados) de um usuário, paginado. */
    Page<EmprestimoResponseDTO> historicoDeEmprestimos(Long usuarioId, Pageable pageable);
}
