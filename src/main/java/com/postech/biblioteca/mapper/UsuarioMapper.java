package com.postech.biblioteca.mapper;

import com.postech.biblioteca.dto.request.UsuarioRequestDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResumoDTO;
import com.postech.biblioteca.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    /**
     * @param senhaCodificada hash bcrypt já calculado pelo service (ver
     *                        {@code UsuarioServiceImpl}) — o mapper nunca lida
     *                        com senha em texto puro nem com o
     *                        {@code PasswordEncoder} diretamente.
     */
    public static Usuario toEntity(UsuarioRequestDTO dto, String senhaCodificada) {
        return Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(senhaCodificada)
                .papel(dto.papel())
                .build();
    }

    /**
     * @param senhaCodificada hash bcrypt da nova senha, ou {@code null} para
     *                        manter a senha atual do usuário (ver
     *                        {@code UsuarioRequestDTO#senha}).
     */
    public static void atualizarEntity(Usuario usuario, UsuarioRequestDTO dto, String senhaCodificada) {
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setPapel(dto.papel());
        if (senhaCodificada != null) {
            usuario.setSenha(senhaCodificada);
        }
    }

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPapel(), usuario.getDataCadastro());
    }

    public static UsuarioResumoDTO toResumoDTO(Usuario usuario) {
        return new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
