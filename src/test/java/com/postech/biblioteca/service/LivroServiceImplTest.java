package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.LivroRequestDTO;
import com.postech.biblioteca.dto.response.LivroResponseDTO;
import com.postech.biblioteca.entity.Livro;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.repository.LivroRepository;
import com.postech.biblioteca.service.impl.LivroServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do CRUD e das regras de validação de livros (ISBN único,
 * cadastro em lote).
 */
@ExtendWith(MockitoExtension.class)
class LivroServiceImplTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroServiceImpl livroService;

    @Test
    void deveCadastrarLivroComQuantidadeDisponivelIgualATotal() {
        var dto = new LivroRequestDTO("Clean Code", "Robert C. Martin", "9780132350884", 3);

        when(livroRepository.findByIsbn(dto.isbn())).thenReturn(Optional.empty());
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocation -> {
            Livro l = invocation.getArgument(0);
            l.setId(1L);
            if (l.getDataCadastro() == null) {
                l.setDataCadastro(Instant.now());
            }
            return l;
        });

        LivroResponseDTO resultado = livroService.cadastrar(dto);

        assertThat(resultado.quantidadeTotal()).isEqualTo(3);
        assertThat(resultado.quantidadeDisponivel()).isEqualTo(3);
        assertThat(resultado.disponivel()).isTrue();
    }

    @Test
    void naoDeveCadastrarLivroComIsbnDuplicado() {
        var dto = new LivroRequestDTO("Clean Code", "Robert C. Martin", "9780132350884", 3);
        Livro existente = Livro.builder().id(5L).isbn(dto.isbn()).build();

        when(livroRepository.findByIsbn(dto.isbn())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> livroService.cadastrar(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISBN");

        verify(livroRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarLivroInexistente() {
        when(livroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livroService.buscarPorId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveCadastrarVariosLivrosEmLote() {
        var dto1 = new LivroRequestDTO("Clean Code", "Robert C. Martin", "9780132350884", 2);
        var dto2 = new LivroRequestDTO("Effective Java", "Joshua Bloch", "9780134685991", 1);

        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.saveAll(any())).thenAnswer(invocation -> {
            List<Livro> livros = invocation.getArgument(0);
            long id = 1L;
            for (Livro l : livros) {
                l.setId(id++);
                l.setDataCadastro(Instant.now());
            }
            return livros;
        });

        List<LivroResponseDTO> resultado = livroService.cadastrarEmLote(List.of(dto1, dto2));

        assertThat(resultado).hasSize(2);
        verify(livroRepository, times(1)).saveAll(any());
    }
}
