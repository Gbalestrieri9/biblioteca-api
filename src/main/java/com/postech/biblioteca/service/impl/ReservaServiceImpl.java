package com.postech.biblioteca.service.impl;

import com.postech.biblioteca.dto.request.ReservaRequestDTO;
import com.postech.biblioteca.dto.response.ReservaResponseDTO;
import com.postech.biblioteca.entity.Livro;
import com.postech.biblioteca.entity.Reserva;
import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.enums.StatusReserva;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.mapper.ReservaMapper;
import com.postech.biblioteca.repository.LivroRepository;
import com.postech.biblioteca.repository.ReservaRepository;
import com.postech.biblioteca.repository.UsuarioRepository;
import com.postech.biblioteca.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Regras de negócio de reserva de livros: permite que um usuário reserve um
 * livro atualmente indisponível (todos os exemplares emprestados) para ser
 * avisado/priorizado quando houver devolução.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public ReservaResponseDTO reservar(ReservaRequestDTO dto) {
        Livro livro = livroRepository.findById(dto.livroId())
                .orElseThrow(() -> ResourceNotFoundException.of("Livro", dto.livroId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> ResourceNotFoundException.of("Usuário", dto.usuarioId()));

        if (livro.isDisponivel()) {
            throw new BusinessException(
                    "O livro '" + livro.getTitulo() + "' possui exemplares disponíveis; realize o empréstimo diretamente.");
        }

        Reserva reserva = Reserva.builder()
                .livro(livro)
                .usuario(usuario)
                .dataReserva(Instant.now())
                .status(StatusReserva.ATIVA)
                .build();

        return ReservaMapper.toResponseDTO(reservaRepository.save(reserva));
    }

    @Override
    public ReservaResponseDTO cancelar(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reserva", reservaId));

        if (reserva.getStatus() != StatusReserva.ATIVA) {
            throw new BusinessException("Somente reservas ativas podem ser canceladas.");
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        return ReservaMapper.toResponseDTO(reservaRepository.save(reserva));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponseDTO> listar(Long usuarioId, Pageable pageable) {
        Page<Reserva> pagina = usuarioId != null
                ? reservaRepository.findByUsuarioId(usuarioId, pageable)
                : reservaRepository.findAll(pageable);
        return pagina.map(ReservaMapper::toResponseDTO);
    }
}
