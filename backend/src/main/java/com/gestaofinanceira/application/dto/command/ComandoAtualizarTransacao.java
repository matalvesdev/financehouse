package com.gestaofinanceira.application.dto.command;

import com.gestaofinanceira.domain.valueobjects.TipoTransacao;
import com.gestaofinanceira.domain.valueobjects.TransacaoId;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando para atualização de transação.
 * 
 * Representa a intenção de atualizar uma transação existente,
 * incluindo IDs de usuário e transação para validação de propriedade.
 */
public record ComandoAtualizarTransacao(
    TransacaoId transacaoId,
    UsuarioId usuarioId,
    BigDecimal valor,
    String descricao,
    String categoria,
    TipoTransacao tipo,
    LocalDate data
) {}