package com.postech.biblioteca.mapper;

import com.postech.biblioteca.dto.request.UsuarioRequestDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResumoDTO;
import com.postech.biblioteca.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toEntity(UsuarioRequestDTO dto) {
        return Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .build();
    }

    public static void atualizarEntity(Usuario usuario, UsuarioRequestDTO dto) {
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
    }

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getDataCadastro());
    }

    public static UsuarioResumoDTO toResumoDTO(Usuario usuario) {
        return new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
