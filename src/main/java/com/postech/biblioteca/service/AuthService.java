package com.postech.biblioteca.service;

import com.postech.biblioteca.dto.request.LoginRequestDTO;
import com.postech.biblioteca.dto.request.RegistrarRequestDTO;
import com.postech.biblioteca.dto.response.TokenResponseDTO;
import com.postech.biblioteca.dto.response.UsuarioResponseDTO;

public interface AuthService {

    TokenResponseDTO login(LoginRequestDTO dto);

    TokenResponseDTO registrar(RegistrarRequestDTO dto);

    UsuarioResponseDTO usuarioAutenticado(String email);
}
