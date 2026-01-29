package com.gestaofinanceira.infrastructure.service;

import com.gestaofinanceira.application.ports.service.ProcessadorPlanilhaPort.*;
import net.jqwik.api.*;
import net.jqwik.time.api.Dates;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de propriedade para o processador de planilhas.
 * 
 * **Validates: Requirements 2.1, 2.2, 2.6**
 */
class ProcessadorPlanilhaPropertyTest {
    
    private final ProcessadorPlanilhaAdapter processador = new ProcessadorPlanilhaAdapter();
    
    /**
     * **Property 5: Valid file processing**
     * 
     * Arquivos válidos devem ser processados com sucesso e produzir dados estruturados.
     * 
     * **Validates: Requirements 2.1, 2.2**
     */
    @Property
    void validFileProcessing(@ForAll("validCsvFiles") MultipartFile arquivo) {
        // Given: um arquivo CSV válido
        
        // When: processamos o arquivo
        DadosPlanilha dados = processador.processarArquivo(arquivo);
        
        // Then: os dados devem ser estruturados corretamente
        assertThat(dados).isNotNull();
        assertThat(dados.nomeArquivo()).isEqualTo(arquivo.getOriginalFilename());
        assertThat(dados.cabecalhos()).containsExactly("data", "valor", "descricao", "categoria", "tipo");
        assertThat(dados.linhas()).isNotEmpty();
        
        // E todas as linhas válidas devem ter 5 campos
        dados.linhas().stream()
            .filter(LinhaPlanilha::valida)
            .forEach(linha -> {
                assertThat(linha.valores()).hasSize(5);
                assertThat(linha.errosValidacao()).isEmpty();
            });
        
        // E deve ser possível extrair transações
        List<TransacaoImportada> transacoes = processador.extrairTransacoes(dados);
        assertThat(transacoes).isNotNull();
        
        // Todas as transações extraídas devem ter dados válidos
        transacoes.forEach(transacao -> {
            assertThat(transacao.data()).isNotNull();
            assertThat(transacao.valor()).isNotNull();
            assertThat(transacao.descricao()).isNotBlank();
            assertThat(transacao.categoria()).isNotBlank();
            assertThat(transacao.tipo()).isIn("RECEITA", "DESPESA");
        });
    }
    
    /**
     * **Property 6: Invalid file rejection**
     * 
     * Arquivos inválidos devem ser rejeitados com mensagens de erro apropriadas.
     * 
     * **Validates: Requirements 2.2, 2.6**
     */
    @Property
    void invalidFileRejection(@ForAll("invalidFiles") MultipartFile arquivo) {
        // Given: um arquivo inválido
        
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
     * 
     * **Validates: Requirements 2.1, 2.6**
     */
    @Property
    void requiredFieldValidation(@ForAll("csvFilesWithMissingFields") MultipartFile arquivo) {
        // Given: um arquivo CSV com campos obrigatórios faltando
        
        // When: processamos o arquivo
        DadosPlanilha dados = processador.processarArquivo(arquivo);
        
        // Then: linhas com campos faltando devem ser inválidas
        boolean temLinhasInvalidas = dados.linhas().stream()
            .anyMatch(linha -> !linha.valida());
        
        assertThat(temLinhasInvalidas).isTrue();
        
        // E deve haver erros de processamento reportados
        assertThat(dados.erros()).isNotEmpty();
        
        // E transações extraídas não devem incluir linhas inválidas
        List<TransacaoImportada> transacoes = processador.extrairTransacoes(dados);
        long linhasValidas = dados.linhas().stream()
            .filter(LinhaPlanilha::valida)
            .count();
        
        // Número de transações deve ser <= número de linhas válidas
        assertThat(transacoes.size()).isLessThanOrEqualTo((int) linhasValidas);
    }
    
    /**
     * Propriedade adicional: Detecção de duplicatas deve ser consistente.
     */
    @Property
    void duplicateDetectionConsistency(
            @ForAll("transacoesImportadas") List<TransacaoImportada> importadas,
            @ForAll("transacoesExistentes") List<TransacaoExistente> existentes) {
        
        // Given: listas de transações importadas e existentes
        
        // When: detectamos duplicatas
        List<DuplicataPotencial> duplicatas = processador.detectarDuplicatas(importadas, existentes);
        
        // Then: resultado deve ser consistente
        assertThat(duplicatas).isNotNull();
        
        // Todas as duplicatas devem ter similaridade >= 0.8
        duplicatas.forEach(duplicata -> {
            assertThat(duplicata.similaridade()).isGreaterThanOrEqualTo(0.8);
            assertThat(duplicata.motivoDeteccao()).isNotBlank();
        });
        
        // Não deve haver duplicatas duplicadas na lista
        long duplicatasUnicas = duplicatas.stream()
            .map(d -> d.transacaoImportada().linhaOrigem() + "-" + d.transacaoExistente().id())
            .distinct()
            .count();
        
        assertThat(duplicatasUnicas).isEqualTo(duplicatas.size());
    }
    
    // Geradores de dados para os testes
    
    @Provide
    Arbitrary<MultipartFile> validCsvFiles() {
        return Arbitraries.integers().between(1, 10)
            .flatMap(numLinhas -> {
                StringBuilder csv = new StringBuilder();
                csv.append("data,valor,descricao,categoria,tipo\n");
                
                for (int i = 0; i < numLinhas; i++) {
                    csv.append("2024-01-").append(String.format("%02d", i + 1)).append(",");
                    csv.append("100.50,");
                    csv.append("Transacao ").append(i + 1).append(",");
                    csv.append("Alimentacao,");
                    csv.append(i % 2 == 0 ? "DESPESA" : "RECEITA");
                    csv.append("\n");
                }
                
                byte[] content = csv.toString().getBytes();
                return Arbitraries.just(new MockMultipartFile(
                    "arquivo",
                    "transacoes.csv",
                    "text/csv",
                    content
                ));
            });
    }
    
    @Provide
    Arbitrary<MultipartFile> invalidFiles() {
        return Arbitraries.oneOf(
            // Arquivo vazio
            Arbitraries.just(new MockMultipartFile(
                "arquivo", "empty.csv", "text/csv", new byte[0]
            )),
            
            // Arquivo muito grande (simulado com tipo MIME inválido)
            Arbitraries.just(new MockMultipartFile(
                "arquivo", "large.txt", "text/plain", "conteudo".getBytes()
            )),
            
            // Arquivo sem cabeçalhos corretos
            Arbitraries.just(new MockMultipartFile(
                "arquivo", "invalid.csv", "text/csv", 
                "campo1,campo2,campo3\nvalor1,valor2,valor3".getBytes()
            )),
            
            // Arquivo nulo (simulado com nome vazio)
            Arbitraries.just(new MockMultipartFile(
                "arquivo", "", "text/csv", "data,valor\n".getBytes()
            ))
        );
    }
    
    @Provide
    Arbitrary<MultipartFile> csvFilesWithMissingFields() {
        return Arbitraries.integers().between(2, 5)
            .flatMap(numLinhas -> {
                StringBuilder csv = new StringBuilder();
                csv.append("data,valor,descricao,categoria,tipo\n");
                
                for (int i = 0; i < numLinhas; i++) {
                    if (i % 2 == 0) {
                        // Linha com campos faltando
                        csv.append("2024-01-01,,Descricao,,DESPESA\n");
                    } else {
                        // Linha válida
                        csv.append("2024-01-02,50.00,Descricao,Categoria,RECEITA\n");
                    }
                }
                
                byte[] content = csv.toString().getBytes();
                return Arbitraries.just(new MockMultipartFile(
                    "arquivo",
                    "mixed.csv",
                    "text/csv",
                    content
                ));
            });
    }
    
    @Provide
    Arbitrary<List<TransacaoImportada>> transacoesImportadas() {
        return transacaoImportada().list().ofMinSize(1).ofMaxSize(5);
    }
    
    @Provide
    Arbitrary<TransacaoImportada> transacaoImportada() {
        return Combinators.combine(
            Arbitraries.integers().between(1, 100),
            Dates.dates().between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
            Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(1000)).ofScale(2),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20),
            Arbitraries.of("Alimentacao", "Transporte", "Lazer", "Saude"),
            Arbitraries.of("RECEITA", "DESPESA")
        ).as((linha, data, valor, descricao, categoria, tipo) -> 
            new TransacaoImportada(linha, data, valor, descricao, categoria, tipo)
        );
    }
    
    @Provide
    Arbitrary<List<TransacaoExistente>> transacoesExistentes() {
        return transacaoExistente().list().ofMinSize(1).ofMaxSize(5);
    }
    
    @Provide
    Arbitrary<TransacaoExistente> transacaoExistente() {
        return Combinators.combine(
            Arbitraries.strings().numeric().ofLength(5),
            Dates.dates().between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)),
            Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(1000)),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20),
            Arbitraries.of("Alimentacao", "Transporte", "Lazer", "Saude")
        ).as(TransacaoExistente::new);
    }
}
