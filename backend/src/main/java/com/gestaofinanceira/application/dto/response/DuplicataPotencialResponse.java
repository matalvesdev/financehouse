package com.gestaofinanceira.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para resposta com duplicata potencial detectada.
 * 
 * Contém informações sobre transações que podem ser duplicatas
 * durante o processo de importação de planilha.
 */
public record DuplicataPotencialResponse(
    int linhaArquivo,
    BigDecimal valor,
    String descricao,
    LocalDate data,
    String categoria,
    String motivoDeteccao,
    TransacaoResponse transacaoExistente
) {}