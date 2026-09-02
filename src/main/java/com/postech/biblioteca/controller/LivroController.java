package com.postech.biblioteca.controller;

import com.postech.biblioteca.config.PaginationUtil;
import com.postech.biblioteca.dto.request.LivroRequestDTO;
import com.postech.biblioteca.dto.response.LivroResponseDTO;
import com.postech.biblioteca.dto.response.PageResponseDTO;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Endpoints de cadastro e consulta de livros (requisitos "Cadastrar livros" e
 * "Consultar livros").
 */
@RestController
@RequestMapping("/api/v1/livros")
@RequiredArgsConstructor
@Tag(name = "Livros", description = "Cadastro, atualização, exclusão e consulta do acervo")
public class LivroController {

    private final LivroService livroService;

    @PostMapping
    @Operation(summary = "Cadastra um novo livro")
    public ResponseEntity<LivroResponseDTO> cadastrar(@Valid @RequestBody LivroRequestDTO dto) {
        LivroResponseDTO criado = livroService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/api/v1/livros/" + criado.id())).body(criado);
    }

    @PostMapping("/lote")
    @Operation(summary = "Cadastra vários livros em uma única operação em lote")
    public ResponseEntity<List<LivroResponseDTO>> cadastrarEmLote(
            @Valid @RequestBody List<@Valid LivroRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BusinessException("A lista de livros não pode ser vazia");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(livroService.cadastrarEmLote(dtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um livro pelo identificador")
    public ResponseEntity<LivroResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(livroService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista livros de forma paginada, com filtros opcionais por título, autor, ISBN e disponibilidade")
    public ResponseEntity<PageResponseDTO<LivroResponseDTO>> buscar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Boolean disponivel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "titulo") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        var pageable = PaginationUtil.of(page, size, sort, direction);
        var resultado = livroService.buscar(titulo, autor, isbn, disponivel, pageable);
        return ResponseEntity.ok(PageResponseDTO.from(resultado, dtoItem -> dtoItem));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um livro")
    public ResponseEntity<LivroResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequestDTO dto) {
        return ResponseEntity.ok(livroService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um livro do acervo")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        livroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
