package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.EmprestimoRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.enums.StatusEmprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmprestimoService {

    EmprestimoResponseDTO emprestar(EmprestimoRequestDTO dto);

    EmprestimoResponseDTO devolver(Long emprestimoId);

    EmprestimoResponseDTO buscarPorId(Long id);

    Page<EmprestimoResponseDTO> listar(StatusEmprestimo status, Long usuarioId, Pageable pageable);
}
