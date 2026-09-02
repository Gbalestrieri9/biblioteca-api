package com.postech.biblioteca.service.impl;

import com.postech.biblioteca.config.AppConstants;
import com.postech.biblioteca.dto.request.EmprestimoRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.entity.Emprestimo;
import com.postech.biblioteca.entity.Livro;
import com.postech.biblioteca.entity.Reserva;
import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.enums.StatusEmprestimo;
import com.postech.biblioteca.enums.StatusReserva;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.mapper.EmprestimoMapper;
import com.postech.biblioteca.repository.EmprestimoRepository;
import com.postech.biblioteca.repository.LivroRepository;
import com.postech.biblioteca.repository.ReservaRepository;
import com.postech.biblioteca.repository.UsuarioRepository;
import com.postech.biblioteca.repository.specification.EmprestimoSpecification;
import com.postech.biblioteca.service.EmprestimoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Regras de negócio de empréstimo e devolução de livros.
 *
 * <p><b>Convenção de data/hora:</b> {@code dataEmprestimo} e {@code
 * dataDevolucao} são {@link Instant} (instante em UTC). A data prevista de
 * devolução é calculada em {@link AppConstants#ZONE_ID} (UTC) somando-se
 * {@link AppConstants#PRAZO_EMPRESTIMO_DIAS} dias corridos à data do
 * empréstimo, usando a API moderna {@code java.time} conforme exigido pelo
 * desafio.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmprestimoServiceImpl implements EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    @Override
    public EmprestimoResponseDTO emprestar(EmprestimoRequestDTO dto) {
        Livro livro = livroRepository.findById(dto.livroId())
                .orElseThrow(() -> ResourceNotFoundException.of("Livro", dto.livroId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> ResourceNotFoundException.of("Usuário", dto.usuarioId()));

        if (!livro.isDisponivel()) {
            throw new BusinessException(
                    "Não há exemplares disponíveis do livro '" + livro.getTitulo() +
                            "' no momento. Utilize o endpoint de reservas para ser avisado quando houver disponibilidade.");
        }

        boolean jaEmprestado = emprestimoRepository.existsByLivroIdAndUsuarioIdAndStatus(
                livro.getId(), usuario.getId(), StatusEmprestimo.ATIVO);
        if (jaEmprestado) {
            throw new BusinessException("O usuário já possui um empréstimo ativo deste livro.");
        }

        Instant agora = Instant.now();
        LocalDate dataPrevista = LocalDate.now(AppConstants.ZONE_ID).plusDays(AppConstants.PRAZO_EMPRESTIMO_DIAS);

        Emprestimo emprestimo = Emprestimo.builder()
                .livro(livro)
                .usuario(usuario)
                .dataEmprestimo(agora)
                .dataPrevistaDevolucao(dataPrevista)
                .status(StatusEmprestimo.ATIVO)
                .build();

        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() - 1);
        livroRepository.save(livro);
        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        // Se o usuário tinha uma reserva ativa para este livro, ela é atendida.
        reservaRepository.findFirstByLivroIdAndUsuarioIdAndStatus(livro.getId(), usuario.getId(), StatusReserva.ATIVA)
                .ifPresent(reserva -> {
                    reserva.setStatus(StatusReserva.ATENDIDA);
                    reservaRepository.save(reserva);
                });

        return EmprestimoMapper.toResponseDTO(salvo);
    }

    @Override
    public EmprestimoResponseDTO devolver(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> ResourceNotFoundException.of("Empréstimo", emprestimoId));

        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new BusinessException("Este empréstimo já foi devolvido.");
        }

        emprestimo.setDataDevolucao(Instant.now());
        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        int novaQuantidade = Math.min(livro.getQuantidadeTotal(), livro.getQuantidadeDisponivel() + 1);
        livro.setQuantidadeDisponivel(novaQuantidade);
        livroRepository.save(livro);

        return EmprestimoMapper.toResponseDTO(emprestimo);
    }

    @Override
    @Transactional(readOnly = true)
    public EmprestimoResponseDTO buscarPorId(Long id) {
        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Empréstimo", id));
        return EmprestimoMapper.toResponseDTO(emprestimo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmprestimoResponseDTO> listar(StatusEmprestimo status, Long usuarioId, Pageable pageable) {
        var specification = EmprestimoSpecification.comFiltros(status, usuarioId);
        return emprestimoRepository.findAll(specification, pageable).map(EmprestimoMapper::toResponseDTO);
    }
}
