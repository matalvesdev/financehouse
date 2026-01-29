package com.gestaofinanceira.application.dto.response;

import java.util.List;

/**
 * DTO para resposta de importação de planilha.
 * 
 * Contém o resultado do processamento da planilha:
 * - Estatísticas de importação
 * - Transações processadas com sucesso
 * - Duplicatas detectadas
 * - Erros encontrados
 */
public record ResultadoImportacaoResponse(
    boolean sucesso,
    int totalLinhasProcessadas,
    int transacoesCriadas,
    int duplicatasDetectadas,
    int errosEncontrados,
    List<TransacaoResponse> transacoesImportadas,
    List<DuplicataPotencialResponse> duplicatasPotenciais,
    List<ErroImportacaoResponse> erros,
    String mensagem
) {}