package com.postech.biblioteca.mapper;

import com.postech.biblioteca.dto.response.ReservaResponseDTO;
import com.postech.biblioteca.entity.Reserva;

public final class ReservaMapper {

    private ReservaMapper() {
    }

    public static ReservaResponseDTO toResponseDTO(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                LivroMapper.toResumoDTO(reserva.getLivro()),
                UsuarioMapper.toResumoDTO(reserva.getUsuario()),
                reserva.getDataReserva(),
                reserva.getStatus()
        );
    }
}
