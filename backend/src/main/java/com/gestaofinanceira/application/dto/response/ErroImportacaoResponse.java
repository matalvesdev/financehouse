package com.gestaofinanceira.application.dto.response;

/**
 * DTO para resposta com erro de importação.
 * 
 * Contém detalhes sobre erros encontrados durante
 * o processamento da planilha de importação.
 */
public record ErroImportacaoResponse(
    int linhaArquivo,
    String campo,
    String valorInvalido,
    String mensagemErro,
    String tipoErro
) {}