package com.postech.biblioteca.service.impl;

import com.postech.biblioteca.dto.request.LivroRequestDTO;
import com.postech.biblioteca.dto.response.LivroResponseDTO;
import com.postech.biblioteca.entity.Livro;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.mapper.LivroMapper;
import com.postech.biblioteca.repository.LivroRepository;
import com.postech.biblioteca.repository.specification.LivroSpecification;
import com.postech.biblioteca.service.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio do CRUD de livros.
 *
 * <p>{@code cadastrarEmLote} atende ao requisito de "operações em lote":
 * todos os livros são validados e persistidos em uma única transação,
 * aproveitando o batch do Hibernate configurado em {@code application.yml}
 * (hibernate.jdbc.batch_size) para reduzir o número de round-trips ao banco.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LivroServiceImpl implements LivroService {

    private final LivroRepository livroRepository;

    @Override
    public LivroResponseDTO cadastrar(LivroRequestDTO dto) {
        validarIsbnUnico(dto.isbn(), null);
        Livro salvo = livroRepository.save(LivroMapper.toEntity(dto));
        return LivroMapper.toResponseDTO(salvo);
    }

    @Override
    public List<LivroResponseDTO> cadastrarEmLote(List<LivroRequestDTO> dtos) {
        dtos.forEach(dto -> validarIsbnUnico(dto.isbn(), null));

        List<Livro> livros = dtos.stream().map(LivroMapper::toEntity).toList();
        List<Livro> salvos = livroRepository.saveAll(livros);

        return salvos.stream().map(LivroMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LivroResponseDTO buscarPorId(Long id) {
        return LivroMapper.toResponseDTO(buscarEntidadePorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LivroResponseDTO> buscar(String titulo, String autor, String isbn, Boolean disponivel,
                                          Pageable pageable) {
        var specification = LivroSpecification.comFiltros(titulo, autor, isbn, disponivel);
        return livroRepository.findAll(specification, pageable).map(LivroMapper::toResponseDTO);
    }

    @Override
    public LivroResponseDTO atualizar(Long id, LivroRequestDTO dto) {
        Livro livro = buscarEntidadePorId(id);
        validarIsbnUnico(dto.isbn(), id);
        LivroMapper.atualizarEntity(livro, dto);
        return LivroMapper.toResponseDTO(livroRepository.save(livro));
    }

    @Override
    public void excluir(Long id) {
        Livro livro = buscarEntidadePorId(id);
        livroRepository.delete(livro);
    }

    private Livro buscarEntidadePorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Livro", id));
    }

    private void validarIsbnUnico(String isbn, Long idAtual) {
        livroRepository.findByIsbn(isbn).ifPresent(existente -> {
            if (idAtual == null || !existente.getId().equals(idAtual)) {
                throw new BusinessException("Já existe um livro cadastrado com o ISBN " + isbn);
            }
        });
    }
}
