package com.gestaofinanceira.infrastructure.service;

import com.gestaofinanceira.application.ports.service.ProcessadorPlanilhaPort.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Manual test to verify the implementation works correctly.
 * This test simulates what the property-based test does.
 */
class ProcessadorPlanilhaManualTest {
    
    private final ProcessadorPlanilhaAdapter processador = new ProcessadorPlanilhaAdapter();
    
    @Test
    void testValidCsvWithMultipleRows() {
        // Create a CSV file similar to what the property test generates
        StringBuilder csv = new StringBuilder();
        csv.append("data,valor,descricao,categoria,tipo\n");
        
        for (int i = 0; i < 10; i++) {
            csv.append("2024-01-").append(String.format("%02d", i + 1)).append(",");
            csv.append("100.50,");
            csv.append("Transacao ").append(i + 1).append(",");
            csv.append("Alimentacao,");
            csv.append(i % 2 == 0 ? "DESPESA" : "RECEITA");
            csv.append("\n");
        }
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivo",
            "transacoes.csv",
            "text/csv",
            csv.toString().getBytes()
        );
        
        // Process the file
        DadosPlanilha dados = processador.processarArquivo(arquivo);
        
        // Verify the data structure
        assertThat(dados).isNotNull();
        assertThat(dados.nomeArquivo()).isEqualTo("transacoes.csv");
        assertThat(dados.cabecalhos()).containsExactly("data", "valor", "descricao", "categoria", "tipo");
        assertThat(dados.linhas()).hasSize(10);
        assertThat(dados.linhas()).isNotEmpty();
        
        // Verify all valid lines have 5 fields
        dados.linhas().stream()
            .filter(LinhaPlanilha::valida)
            .forEach(linha -> {
                assertThat(linha.valores()).hasSize(5);
                assertThat(linha.errosValidacao()).isEmpty();
            });
        
        // Extract transactions
        List<TransacaoImportada> transacoes = processador.extrairTransacoes(dados);
        assertThat(transacoes).isNotNull();
        assertThat(transacoes).hasSize(10);
        
        // Verify all transactions have valid data
        transacoes.forEach(transacao -> {
            assertThat(transacao.data()).isNotNull();
            assertThat(transacao.valor()).isNotNull();
            assertThat(transacao.descricao()).isNotBlank();
            assertThat(transacao.categoria()).isNotBlank();
            assertThat(transacao.tipo()).isIn("RECEITA", "DESPESA");
        });
        
        // Verify specific transaction data
        TransacaoImportada primeira = transacoes.get(0);
        assertThat(primeira.data()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(primeira.valor()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(primeira.descricao()).isEqualTo("Transacao 1");
        assertThat(primeira.categoria()).isEqualTo("Alimentacao");
        assertThat(primeira.tipo()).isEqualTo("DESPESA");
    }
    
    @Test
    void testInvalidFileWithWrongHeaders() {
        MockMultipartFile arquivo = new MockMultipartFile(
            "arquivo", "invalid.csv", "text/csv", 
            "campo1,campo2,campo3\nvalor1,valor2,valor3".getBytes()
        );
        
        ResultadoValidacao resultado = processador.validarArquivo(arquivo);
        assertThat(resultado.valido()).isTrue(); // File format is valid
        
        // But processing should fail due to missing headers
        assertThatThrownBy(() -> processador.processarArquivo(arquivo))
            .isInstanceOf(ProcessamentoPlanilhaException.class)
            .hasMessageContaining("Arquivo inválido");
    }
    
    @Test
    void testDuplicateDetection() {
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
        
        List<DuplicataPotencial> duplicatas = processador.detectarDuplicatas(
            List.of(importada), 
            List.of(existente)
        );
        
        assertThat(duplicatas).hasSize(1);
        assertThat(duplicatas.get(0).similaridade()).isGreaterThanOrEqualTo(0.8);
        assertThat(duplicatas.get(0).motivoDeteccao()).isNotBlank();
    }
}
