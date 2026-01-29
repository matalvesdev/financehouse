package com.gestaofinanceira.application.dto.command;

import com.gestaofinanceira.domain.valueobjects.PeriodoOrcamento;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando para criação de orçamento.
 * 
 * Representa a intenção de criar um novo orçamento para
 * controle de gastos por categoria.
 */
public record ComandoCriarOrcamento(
    UsuarioId usuarioId,
    String categoria,
    BigDecimal limite,
    PeriodoOrcamento periodo,
    LocalDate inicioVigencia
) {}