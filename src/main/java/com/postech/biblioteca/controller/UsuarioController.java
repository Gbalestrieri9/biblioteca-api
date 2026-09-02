package com.postech.biblioteca.controller;

import com.postech.biblioteca.config.PaginationUtil;
import com.postech.biblioteca.dto.request.UsuarioRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.dto.response.PageResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import com.postech.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Endpoints de gestão de usuários (requisito "Gerir usuários"), incluindo o
 * histórico de empréstimos de cada usuário.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro, atualização, exclusão e consulta de usuários da biblioteca")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Cadastra um novo usuário")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.cadastrar(dto);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário pelo identificador")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista usuários de forma paginada, com filtro opcional por nome")
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        var pageable = PaginationUtil.of(page, size, sort, direction);
        var resultado = usuarioService.listar(nome, pageable);
        return ResponseEntity.ok(PageResponseDTO.from(resultado, dtoItem -> dtoItem));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um usuário")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um usuário")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/emprestimos")
    @Operation(summary = "Lista o histórico de empréstimos de um usuário, de forma paginada")
    public ResponseEntity<PageResponseDTO<EmprestimoResponseDTO>> historicoDeEmprestimos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PaginationUtil.of(page, size, "dataEmprestimo", "desc");
        var resultado = usuarioService.historicoDeEmprestimos(id, pageable);
        return ResponseEntity.ok(PageResponseDTO.from(resultado, dtoItem -> dtoItem));
    }
}
