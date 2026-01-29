package com.gestaofinanceira.application.dto.command;

import com.gestaofinanceira.domain.valueobjects.TipoTransacao;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando para criação de transação.
 * 
 * Representa a intenção de criar uma nova transação financeira,
 * incluindo o ID do usuário proprietário.
 */
public record ComandoCriarTransacao(
    UsuarioId usuarioId,
    BigDecimal valor,
    String descricao,
    String categoria,
    TipoTransacao tipo,
    LocalDate data
) {}