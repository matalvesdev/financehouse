package com.gestaofinanceira.application.ports.service;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Port para processamento de planilhas de importação.
 * 
 * Define as operações necessárias para processar arquivos Excel/CSV
 * contendo dados financeiros históricos do usuário.
 */
public interface ProcessadorPlanilhaPort {
    
    /**
     * Processa um arquivo de planilha e extrai os dados financeiros.
     * 
     * @param arquivo o arquivo de planilha (Excel ou CSV)
     * @return dados processados da planilha
     * @throws ProcessamentoPlanilhaException se houver erro no processamento
     */
    DadosPlanilha processarArquivo(MultipartFile arquivo);
    
    /**
     * Extrai transações dos dados processados da planilha.
     * 
     * @param dados os dados processados da planilha
     * @return lista de transações extraídas
     */
    List<TransacaoImportada> extrairTransacoes(DadosPlanilha dados);
    
    /**
     * Detecta possíveis duplicatas nas transações importadas.
     * 
     * @param transacoes lista de transações a serem verificadas
     * @param transacoesExistentes lista de transações já existentes no sistema
     * @return lista de duplicatas potenciais detectadas
     */
    List<DuplicataPotencial> detectarDuplicatas(
        List<TransacaoImportada> transacoes, 
        List<TransacaoExistente> transacoesExistentes
    );
    
    /**
     * Valida o formato e conteúdo do arquivo de planilha.
     * 
     * @param arquivo o arquivo a ser validado
     * @return resultado da validação
     */
    ResultadoValidacao validarArquivo(MultipartFile arquivo);
    
    /**
     * Representa os dados processados de uma planilha.
     */
    record DadosPlanilha(
        String nomeArquivo,
        int totalLinhas,
        List<String> cabecalhos,
        List<LinhaPlanilha> linhas,
        List<ErroProcessamento> erros
    ) {}
    
    /**
     * Representa uma linha processada da planilha.
     */
    record LinhaPlanilha(
        int numeroLinha,
        List<String> valores,
        boolean valida,
        List<String> errosValidacao
    ) {}
    
    /**
     * Representa uma transação extraída da planilha.
     */
    record TransacaoImportada(
        int linhaOrigem,
        LocalDate data,
        BigDecimal valor,
        String descricao,
        String categoria,
        String tipo
    ) {}
    
    /**
     * Representa uma transação existente no sistema para comparação.
     */
    record TransacaoExistente(
        String id,
        LocalDate data,
        BigDecimal valor,
        String descricao,
        String categoria
    ) {}
    
    /**
     * Representa uma duplicata potencial detectada.
     */
    record DuplicataPotencial(
        TransacaoImportada transacaoImportada,
        TransacaoExistente transacaoExistente,
        String motivoDeteccao,
        double similaridade
    ) {}
    
    /**
     * Representa um erro de processamento.
     */
    record ErroProcessamento(
        int linha,
        String campo,
        String valor,
        String mensagem,
        TipoErro tipo
    ) {}
    
    /**
     * Representa o resultado da validação de arquivo.
     */
    record ResultadoValidacao(
        boolean valido,
        List<String> erros,
        List<String> avisos,
        FormatoArquivo formato
    ) {}
    
    /**
     * Tipos de erro de processamento.
     */
    enum TipoErro {
        FORMATO_INVALIDO,
        CAMPO_OBRIGATORIO,
        VALOR_INVALIDO,
        DATA_INVALIDA,
        CATEGORIA_INVALIDA
    }
    
    /**
     * Formatos de arquivo suportados.
     */
    enum FormatoArquivo {
        EXCEL_XLSX,
        CSV,
        DESCONHECIDO
    }
    
    /**
     * Exceção para erros de processamento de planilha.
     */
    class ProcessamentoPlanilhaException extends RuntimeException {
        public ProcessamentoPlanilhaException(String message) {
            super(message);
        }
        
        public ProcessamentoPlanilhaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}