package com.postech.biblioteca.mapper;

import com.postech.biblioteca.config.AppConstants;
import com.postech.biblioteca.dto.response.EmprestimoResponseDTO;
import com.postech.biblioteca.dto.response.RelatorioEmprestimoAtivoDTO;
import com.postech.biblioteca.entity.Emprestimo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class EmprestimoMapper {

    private EmprestimoMapper() {
    }

    public static EmprestimoResponseDTO toResponseDTO(Emprestimo emprestimo) {
        LocalDate hoje = LocalDate.now(AppConstants.ZONE_ID);
        boolean atrasado = emprestimo.isAtrasado(hoje);
        long diasAtraso = atrasado ? ChronoUnit.DAYS.between(emprestimo.getDataPrevistaDevolucao(), hoje) : 0;

        return new EmprestimoResponseDTO(
                emprestimo.getId(),
                LivroMapper.toResumoDTO(emprestimo.getLivro()),
                UsuarioMapper.toResumoDTO(emprestimo.getUsuario()),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevistaDevolucao(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus(),
                atrasado,
                diasAtraso
        );
    }

    public static RelatorioEmprestimoAtivoDTO toRelatorioAtivoDTO(Emprestimo emprestimo) {
        LocalDate hoje = LocalDate.now(AppConstants.ZONE_ID);
        boolean atrasado = emprestimo.isAtrasado(hoje);
        long diasAtraso = atrasado ? ChronoUnit.DAYS.between(emprestimo.getDataPrevistaDevolucao(), hoje) : 0;

        return new RelatorioEmprestimoAtivoDTO(
                emprestimo.getId(),
                LivroMapper.toResumoDTO(emprestimo.getLivro()),
                UsuarioMapper.toResumoDTO(emprestimo.getUsuario()),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevistaDevolucao(),
                atrasado,
                diasAtraso
        );
    }
}
