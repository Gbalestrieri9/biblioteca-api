package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.ReservaRequestDTO;
import com.postech.biblioteca.dto.response.ReservaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {

    ReservaResponseDTO reservar(ReservaRequestDTO dto);

    ReservaResponseDTO cancelar(Long reservaId);

    Page<ReservaResponseDTO> listar(Long usuarioId, Pageable pageable);
}
