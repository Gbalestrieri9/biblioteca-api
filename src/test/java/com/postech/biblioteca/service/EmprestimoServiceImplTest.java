package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.EmprestimoRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.entity.Emprestimo;
import com.postech.biblioteca.entity.Livro;
import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.enums.StatusEmprestimo;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.repository.EmprestimoRepository;
import com.postech.biblioteca.repository.LivroRepository;
import com.postech.biblioteca.repository.ReservaRepository;
import com.postech.biblioteca.repository.UsuarioRepository;
import com.postech.biblioteca.service.impl.EmprestimoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários das regras de negócio de empréstimo/devolução: cálculo do
 * prazo, controle de disponibilidade do acervo e transições de status.
 */
@ExtendWith(MockitoExtension.class)
class EmprestimoServiceImplTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;
    @Mock
    private LivroRepository livroRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private EmprestimoServiceImpl emprestimoService;

    private Livro livro;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        livro = Livro.builder()
                .id(1L)
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .isbn("9780132350884")
                .quantidadeTotal(2)
                .quantidadeDisponivel(2)
                .dataCadastro(Instant.now())
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .nome("Ana Souza")
                .email("ana.souza@example.com")
                .dataCadastro(Instant.now())
                .build();
    }

    @Test
    void deveEmprestarLivroDisponivelECalcularPrazoDeQuatorzeDias() {
        var dto = new EmprestimoRequestDTO(livro.getId(), usuario.getId());

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByLivroIdAndUsuarioIdAndStatus(1L, 1L, StatusEmprestimo.ATIVO))
                .thenReturn(false);
        when(reservaRepository.findFirstByLivroIdAndUsuarioIdAndStatus(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> {
            Emprestimo e = invocation.getArgument(0);
            e.setId(100L);
            return e;
        });

        EmprestimoResponseDTO resultado = emprestimoService.emprestar(dto);

        assertThat(resultado.status()).isEqualTo(StatusEmprestimo.ATIVO);
        assertThat(resultado.dataPrevistaDevolucao())
                .isEqualTo(resultado.dataEmprestimo().atZone(com.postech.biblioteca.config.AppConstants.ZONE_ID)
                        .toLocalDate().plusDays(14));
        assertThat(livro.getQuantidadeDisponivel()).isEqualTo(1);
        verify(livroRepository).save(livro);
    }

    @Test
    void naoDeveEmprestarLivroSemExemplarDisponivel() {
        livro.setQuantidadeDisponivel(0);
        var dto = new EmprestimoRequestDTO(livro.getId(), usuario.getId());

        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> emprestimoService.emprestar(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disponíveis");

        verify(emprestimoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoEmprestarLivroInexistente() {
        var dto = new EmprestimoRequestDTO(99L, usuario.getId());
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emprestimoService.emprestar(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveDevolverEmprestimoEIncrementarDisponibilidadeDoLivro() {
        livro.setQuantidadeDisponivel(1);
        Emprestimo emprestimo = Emprestimo.builder()
                .id(10L)
                .livro(livro)
                .usuario(usuario)
                .dataEmprestimo(Instant.now())
                .dataPrevistaDevolucao(java.time.LocalDate.now().plusDays(14))
                .status(StatusEmprestimo.ATIVO)
                .build();

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);

        EmprestimoResponseDTO resultado = emprestimoService.devolver(10L);

        assertThat(resultado.status()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(livro.getQuantidadeDisponivel()).isEqualTo(2);
    }

    @Test
    void naoDeveDevolverEmprestimoJaDevolvido() {
        Emprestimo emprestimo = Emprestimo.builder()
                .id(10L)
                .livro(livro)
                .usuario(usuario)
                .dataEmprestimo(Instant.now())
                .dataDevolucao(Instant.now())
                .dataPrevistaDevolucao(java.time.LocalDate.now())
                .status(StatusEmprestimo.DEVOLVIDO)
                .build();

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimo));

        assertThatThrownBy(() -> emprestimoService.devolver(10L))
                .isInstanceOf(BusinessException.class);
    }
}
