package com.gestaofinanceira.infrastructure.service;

import com.gestaofinanceira.application.ports.service.ProcessadorPlanilhaPort.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes simples para o processador de planilhas.
 * 
 * **Validates: Requirements 2.1, 2.2, 2.6**
 */
class ProcessadorPlanilhaSimpleTest {
    
    private final ProcessadorPlanilhaAdapter processador = new ProcessadorPlanilhaAdapter();
    
    /**
     * **Property 5: Valid file processing**
     * 
     * Arquivos válidos devem ser processados com sucesso.
     */
    @Test
    void validCsvFileProcessing() {
        // Given: um arquivo CSV válido
        String csvContent = """
            data,valor,descricao,categoria,tipo
            2024-01-01,100.50,Compra supermercado,Alimentacao,DESPESA
            2024-01-02,2500.00,Salario,Trabalho,RECEITA
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivo", "transacoes.csv", "text/csv", csvContent.getBytes()
        );
        
        // When: processamos o arquivo
        DadosPlanilha dados = processador.processarArquivo(arquivo);
        
        // Then: os dados devem ser estruturados corretamente
        assertThat(dados).isNotNull();
        assertThat(dados.nomeArquivo()).isEqualTo("transacoes.csv");
        assertThat(dados.cabecalhos()).containsExactly("data", "valor", "descricao", "categoria", "tipo");
        assertThat(dados.linhas()).hasSize(2);
        
        // E deve ser possível extrair transações
        List<TransacaoImportada> transacoes = processador.extrairTransacoes(dados);
        assertThat(transacoes).hasSize(2);
        
        TransacaoImportada primeira = transacoes.get(0);
        assertThat(primeira.data()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(primeira.valor()).isEqualTo(new BigDecimal("100.50"));
        assertThat(primeira.descricao()).isEqualTo("Compra supermercado");
        assertThat(primeira.categoria()).isEqualTo("Alimentacao");
        assertThat(primeira.tipo()).isEqualTo("DESPESA");
    }
    
    /**
     * **Property 6: Invalid file rejection**
     * 
     * Arquivos inválidos devem ser rejeitados.
     */
    @Test
    void invalidFileRejection() {
        // Given: um arquivo vazio
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivo", "empty.csv", "text/csv", new byte[0]
        );
        
        // When: tentamos validar o arquivo
        ResultadoValidacao resultado = processador.validarArquivo(arquivo);
        
        // Then: a validação deve falhar
        assertThat(resultado.valido()).isFalse();
        assertThat(resultado.erros()).isNotEmpty();
        
        // E tentar processar deve lançar exceção
        assertThatThrownBy(() -> processador.processarArquivo(arquivo))
            .isInstanceOf(ProcessamentoPlanilhaException.class)
            .hasMessageContaining("Arquivo inválido");
    }
    
    /**
     * **Property 7: Required field validation**
     * 
     * Linhas com campos obrigatórios vazios devem ser marcadas como inválidas.
     */
    @Test
    void requiredFieldValidation() {
        // Given: um arquivo CSV com campos obrigatórios faltando
        String csvContent = """
            data,valor,descricao,categoria,tipo
            2024-01-01,,Descricao sem valor,,DESPESA
            2024-01-02,50.00,Descricao completa,Categoria,RECEITA
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivo", "mixed.csv", "text/csv", csvContent.getBytes()
        );
        
        // When: processamos o arquivo
        DadosPlanilha dados = processador.processarArquivo(arquivo);
        
        // Then: deve haver linhas inválidas
        boolean temLinhasInvalidas = dados.linhas().stream()
            .anyMatch(linha -> !linha.valida());
        
        assertThat(temLinhasInvalidas).isTrue();
        assertThat(dados.erros()).isNotEmpty();
        
        // E transações extraídas não devem incluir linhas inválidas
        List<TransacaoImportada> transacoes = processador.extrairTransacoes(dados);
        assertThat(transacoes).hasSize(1); // Apenas a linha válida
        
        TransacaoImportada transacao = transacoes.get(0);
        assertThat(transacao.valor()).isEqualTo(new BigDecimal("50.00"));
        assertThat(transacao.descricao()).isEqualTo("Descricao completa");
    }
    
    /**
     * Teste de detecção de duplicatas.
     */
    @Test
    void duplicateDetection() {
        // Given: transações importadas e existentes similares
        TransacaoImportada importada = new TransacaoImportada(
            1,
            LocalDate.of(2024, 1, 1),
            new BigDecimal("100.00"),
            "Compra supermercado",
            "Alimentacao",
            "DESPESA"
        );
        
        TransacaoExistente existente = new TransacaoExistente(
            "123",
            LocalDate.of(2024, 1, 1),
            new BigDecimal("100.00"),
            "Compra supermercado",
            "Alimentacao"
        );
        
        // When: detectamos duplicatas
        List<DuplicataPotencial> duplicatas = processador.detectarDuplicatas(
            List.of(importada), 
            List.of(existente)
        );
        
        // Then: deve detectar a duplicata
        assertThat(duplicatas).hasSize(1);
        
        DuplicataPotencial duplicata = duplicatas.get(0);
        assertThat(duplicata.similaridade()).isGreaterThanOrEqualTo(0.8);
        assertThat(duplicata.motivoDeteccao()).contains("mesma data", "mesmo valor");
    }
}