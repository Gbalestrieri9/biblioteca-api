package com.postech.biblioteca.repository;

import com.postech.biblioteca.entity.Reserva;
import com.postech.biblioteca.enums.StatusReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Page<Reserva> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<Reserva> findByStatus(StatusReserva status, Pageable pageable);

    List<Reserva> findByLivroIdAndStatusOrderByDataReservaAsc(Long livroId, StatusReserva status);

    Optional<Reserva> findFirstByLivroIdAndUsuarioIdAndStatus(Long livroId, Long usuarioId, StatusReserva status);
}
