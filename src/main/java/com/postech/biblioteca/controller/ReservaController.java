package com.postech.biblioteca.controller;

import com.postech.biblioteca.config.PaginationUtil;
import com.postech.biblioteca.dto.request.ReservaRequestDTO;
import com.postech.biblioteca.dto.response.PageResponseDTO;
import com.postech.biblioteca.dto.response.ReservaResponseDTO;
import com.postech.biblioteca.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Endpoints de reserva de livros — parte do requisito "Consultar livros":
 * "os usuários possam [...] realizar reservas".
 */
@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Reserva de livros indisponíveis no momento")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    @Operation(summary = "Reserva um livro atualmente indisponível")
    public ResponseEntity<ReservaResponseDTO> reservar(@Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO criada = reservaService.reservar(dto);
        return ResponseEntity.created(URI.create("/api/v1/reservas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela uma reserva ativa")
    public ResponseEntity<ReservaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }

    @GetMapping
    @Operation(summary = "Lista reservas de forma paginada, com filtro opcional por usuário")
    public ResponseEntity<PageResponseDTO<ReservaResponseDTO>> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PaginationUtil.of(page, size, "dataReserva", "desc");
        var resultado = reservaService.listar(usuarioId, pageable);
        return ResponseEntity.ok(PageResponseDTO.from(resultado, dtoItem -> dtoItem));
    }
}
