package com.postech.biblioteca.controller;

import com.postech.biblioteca.config.PaginationUtil;
import com.postech.biblioteca.dto.response.PageResponseDTO;
import com.postech.biblioteca.dto.response.RelatorioEmprestimoAtivoDTO;
import com.postech.biblioteca.dto.response.RelatorioLivroMaisEmprestadoDTO;
import com.postech.biblioteca.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de relatórios de uso da biblioteca (requisito "Relatórios").
 */
@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatórios de uso da biblioteca")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/livros-mais-emprestados")
    @Operation(summary = "Lista dos 20 livros mais emprestados da biblioteca")
    public ResponseEntity<List<RelatorioLivroMaisEmprestadoDTO>> livrosMaisEmprestados() {
        return ResponseEntity.ok(relatorioService.livrosMaisEmprestados());
    }

    @GetMapping("/emprestimos-ativos")
    @Operation(summary = "Lista dos livros emprestados no momento com a previsão de devolução")
    public ResponseEntity<PageResponseDTO<RelatorioEmprestimoAtivoDTO>> emprestimosAtivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PaginationUtil.of(page, size, "dataPrevistaDevolucao", "asc");
        var resultado = relatorioService.emprestimosAtivos(pageable);
        return ResponseEntity.ok(PageResponseDTO.from(resultado, dtoItem -> dtoItem));
    }
}
