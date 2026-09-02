package com.postech.biblioteca.service.impl;

import com.postech.biblioteca.dto.request.UsuarioRequestDTO;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import com.postech.biblioteca.entity.Usuario;
import com.postech.biblioteca.exception.BusinessException;
import com.postech.biblioteca.exception.ResourceNotFoundException;
import com.postech.biblioteca.mapper.EmprestimoMapper;
import com.postech.biblioteca.mapper.UsuarioMapper;
import com.postech.biblioteca.repository.EmprestimoRepository;
import com.postech.biblioteca.repository.UsuarioRepository;
import com.postech.biblioteca.repository.specification.UsuarioSpecification;
import com.postech.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    @Override
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        validarEmailUnico(dto.email(), null);
        Usuario salvo = usuarioRepository.save(UsuarioMapper.toEntity(dto));
        return UsuarioMapper.toResponseDTO(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioMapper.toResponseDTO(buscarEntidadePorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(String nome, Pageable pageable) {
        var specification = UsuarioSpecification.comNome(nome);
        return usuarioRepository.findAll(specification, pageable).map(UsuarioMapper::toResponseDTO);
    }

    @Override
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarEntidadePorId(id);
        validarEmailUnico(dto.email(), id);
        UsuarioMapper.atualizarEntity(usuario, dto);
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Override
    public void excluir(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmprestimoResponseDTO> historicoDeEmprestimos(Long usuarioId, Pageable pageable) {
        buscarEntidadePorId(usuarioId);
        return emprestimoRepository.findByUsuarioId(usuarioId, pageable).map(EmprestimoMapper::toResponseDTO);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuário", id));
    }

    private void validarEmailUnico(String email, Long idAtual) {
        usuarioRepository.findByEmail(email).ifPresent(existente -> {
            if (idAtual == null || !existente.getId().equals(idAtual)) {
                throw new BusinessException("Já existe um usuário cadastrado com o e-mail " + email);
            }
        });
    }
}
