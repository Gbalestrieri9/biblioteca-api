package com.postech.biblioteca.controller;

import com.postech.biblioteca.config.PaginationUtil;
import com.postech.biblioteca.dto.request.EmprestimoRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.dto.response.PageResponseDTO;
import com.postech.biblioteca.enums.StatusEmprestimo;
import com.postech.biblioteca.service.EmprestimoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Endpoints de empréstimo e devolução de livros (requisito "Emprestar e
 * devolver"), com cálculo automático do prazo de devolução via {@code
 * java.time}.
 */
@RestController
@RequestMapping("/api/v1/emprestimos")
@RequiredArgsConstructor
@Tag(name = "Empréstimos", description = "Registro de empréstimos e devoluções de livros")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping
    @Operation(summary = "Registra o empréstimo de um livro a um usuário")
    public ResponseEntity<EmprestimoResponseDTO> emprestar(@Valid @RequestBody EmprestimoRequestDTO dto) {
        EmprestimoResponseDTO criado = emprestimoService.emprestar(dto);
        return ResponseEntity.created(URI.create("/api/v1/emprestimos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}/devolucao")
    @Operation(summary = "Registra a devolução de um livro emprestado")
    public ResponseEntity<EmprestimoResponseDTO> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(emprestimoService.devolver(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um empréstimo pelo identificador")
    public ResponseEntity<EmprestimoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(emprestimoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista empréstimos de forma paginada, com filtros opcionais por status e usuário")
    public ResponseEntity<PageResponseDTO<EmprestimoResponseDTO>> listar(
            @RequestParam(required = false) StatusEmprestimo status,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PaginationUtil.of(page, size, "dataEmprestimo", "desc");
        var resultado = emprestimoService.listar(status, usuarioId, pageable);
        return ResponseEntity.ok(PageResponseDTO.from(resultado, dtoItem -> dtoItem));
    }
}
