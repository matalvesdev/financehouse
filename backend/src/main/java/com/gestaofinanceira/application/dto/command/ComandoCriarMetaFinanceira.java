package com.gestaofinanceira.application.dto.command;

import com.gestaofinanceira.domain.valueobjects.TipoMeta;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando para criação de meta financeira.
 * 
 * Representa a intenção de criar uma nova meta financeira
 * com valor alvo e prazo definidos.
 */
public record ComandoCriarMetaFinanceira(
    UsuarioId usuarioId,
    String nome,
    BigDecimal valorAlvo,
    LocalDate prazo,
    TipoMeta tipo
) {}