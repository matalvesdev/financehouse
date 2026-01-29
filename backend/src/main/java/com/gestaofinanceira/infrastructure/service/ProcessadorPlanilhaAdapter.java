package com.gestaofinanceira.infrastructure.service;

import com.gestaofinanceira.application.ports.service.ProcessadorPlanilhaPort;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do processador de planilhas para Excel e CSV.
 * 
 * Suporta importação de dados financeiros de arquivos Excel (.xlsx) e CSV,
 * com validação de formato, detecção de duplicatas e tratamento de erros.
 */
@Service
public class ProcessadorPlanilhaAdapter implements ProcessadorPlanilhaPort {
    
    private static final Set<String> TIPOS_MIME_EXCEL = Set.of(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel"
    );
    
    private static final Set<String> TIPOS_MIME_CSV = Set.of(
        "text/csv",
        "application/csv",
        "text/plain"
    );
    
    private static final List<DateTimeFormatter> FORMATOS_DATA = List.of(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );
    
    private static final Set<String> CABECALHOS_ESPERADOS = Set.of(
        "data", "valor", "descricao", "categoria", "tipo"
    );
    
    private static final double LIMIAR_SIMILARIDADE = 0.8;
    
    @Override
    public DadosPlanilha processarArquivo(MultipartFile arquivo) {
        ResultadoValidacao validacao = validarArquivo(arquivo);
        
        if (!validacao.valido()) {
            throw new ProcessamentoPlanilhaException(
                "Arquivo inválido: " + String.join(", ", validacao.erros())
            );
        }
        
        try {
            return switch (validacao.formato()) {
                case EXCEL_XLSX -> processarExcel(arquivo);
                case CSV -> processarCsv(arquivo);
                default -> throw new ProcessamentoPlanilhaException(
                    "Formato de arquivo não suportado: " + validacao.formato()
                );
            };
        } catch (IOException e) {
            throw new ProcessamentoPlanilhaException("Erro ao processar arquivo", e);
        }
    }
    
    @Override
    public List<TransacaoImportada> extrairTransacoes(DadosPlanilha dados) {
        List<TransacaoImportada> transacoes = new ArrayList<>();
        
        for (LinhaPlanilha linha : dados.linhas()) {
            if (!linha.valida() || linha.valores().size() < 5) {
                continue;
            }
            
            try {
                LocalDate data = parseData(linha.valores().get(0));
                BigDecimal valor = parseValor(linha.valores().get(1));
                String descricao = linha.valores().get(2).trim();
                String categoria = linha.valores().get(3).trim();
                String tipo = linha.valores().get(4).trim().toUpperCase();
                
                // Validar tipo de transação
                if (!tipo.equals("RECEITA") && !tipo.equals("DESPESA")) {
                    continue;
                }
                
                transacoes.add(new TransacaoImportada(
                    linha.numeroLinha(),
                    data,
                    valor,
                    descricao,
                    categoria,
                    tipo
                ));
                
            } catch (Exception e) {
                // Linha inválida, pular
                continue;
            }
        }
        
        return transacoes;
    }
    
    @Override
    public List<DuplicataPotencial> detectarDuplicatas(
            List<TransacaoImportada> transacoes, 
            List<TransacaoExistente> transacoesExistentes) {
        
        List<DuplicataPotencial> duplicatas = new ArrayList<>();
        
        for (TransacaoImportada importada : transacoes) {
            for (TransacaoExistente existente : transacoesExistentes) {
                double similaridade = calcularSimilaridade(importada, existente);
                
                if (similaridade >= LIMIAR_SIMILARIDADE) {
                    String motivo = determinarMotivoDeteccao(importada, existente);
                    
                    duplicatas.add(new DuplicataPotencial(
                        importada,
                        existente,
                        motivo,
                        similaridade
                    ));
                }
            }
        }
        
        return duplicatas;
    }
    
    @Override
    public ResultadoValidacao validarArquivo(MultipartFile arquivo) {
        List<String> erros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        
        if (arquivo == null || arquivo.isEmpty()) {
            erros.add("Arquivo não fornecido ou está vazio");
            return new ResultadoValidacao(false, erros, avisos, FormatoArquivo.DESCONHECIDO);
        }
        
        // Validar tamanho do arquivo (máximo 10MB)
        if (arquivo.getSize() > 10 * 1024 * 1024) {
            erros.add("Arquivo muito grande. Tamanho máximo permitido: 10MB");
        }
        
        // Detectar formato do arquivo
        FormatoArquivo formato = detectarFormato(arquivo);
        
        if (formato == FormatoArquivo.DESCONHECIDO) {
            erros.add("Formato de arquivo não suportado. Use Excel (.xlsx) ou CSV");
        }
        
        // Validar nome do arquivo
        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            avisos.add("Nome do arquivo não informado");
        }
        
        return new ResultadoValidacao(erros.isEmpty(), erros, avisos, formato);
    }
    
    private DadosPlanilha processarExcel(MultipartFile arquivo) throws IOException {
        List<LinhaPlanilha> linhas = new ArrayList<>();
        List<ErroProcessamento> erros = new ArrayList<>();
        List<String> cabecalhos = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(arquivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Processar cabeçalhos
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    cabecalhos.add(getCellValueAsString(cell).toLowerCase());
                }
            }
            
            // Validar cabeçalhos
            validarCabecalhos(cabecalhos, erros);
            
            // Processar linhas de dados
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                List<String> valores = new ArrayList<>();
                List<String> errosLinha = new ArrayList<>();
                
                for (int j = 0; j < cabecalhos.size() && j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    String valor = getCellValueAsString(cell);
                    valores.add(valor);
                    
                    // Validar campos obrigatórios
                    if (j < 5 && (valor == null || valor.trim().isEmpty())) {
                        errosLinha.add("Campo obrigatório vazio na coluna " + (j + 1));
                    }
                }
                
                boolean linhaValida = errosLinha.isEmpty() && valores.size() >= 5;
                linhas.add(new LinhaPlanilha(i + 1, valores, linhaValida, errosLinha));
                
                // Adicionar erros de processamento
                for (String erro : errosLinha) {
                    erros.add(new ErroProcessamento(
                        i + 1, "", "", erro, TipoErro.CAMPO_OBRIGATORIO
                    ));
                }
            }
        }
        
        return new DadosPlanilha(
            arquivo.getOriginalFilename(),
            linhas.size(),
            cabecalhos,
            linhas,
            erros
        );
    }
    
    private DadosPlanilha processarCsv(MultipartFile arquivo) throws IOException {
        List<LinhaPlanilha> linhas = new ArrayList<>();
        List<ErroProcessamento> erros = new ArrayList<>();
        List<String> cabecalhos = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream()))) {
            
            String linha;
            int numeroLinha = 0;
            
            // Processar cabeçalhos
            if ((linha = reader.readLine()) != null) {
                cabecalhos = Arrays.stream(linha.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
                
                validarCabecalhos(cabecalhos, erros);
                numeroLinha++;
            }
            
            // Processar linhas de dados
            while ((linha = reader.readLine()) != null) {
                numeroLinha++;
                
                List<String> valores = Arrays.stream(linha.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
                
                List<String> errosLinha = new ArrayList<>();
                
                // Validar campos obrigatórios
                for (int i = 0; i < Math.min(5, valores.size()); i++) {
                    if (valores.get(i).isEmpty()) {
                        errosLinha.add("Campo obrigatório vazio na coluna " + (i + 1));
                    }
                }
                
                boolean linhaValida = errosLinha.isEmpty() && valores.size() >= 5;
                linhas.add(new LinhaPlanilha(numeroLinha, valores, linhaValida, errosLinha));
                
                // Adicionar erros de processamento
                for (String erro : errosLinha) {
                    erros.add(new ErroProcessamento(
                        numeroLinha, "", "", erro, TipoErro.CAMPO_OBRIGATORIO
                    ));
                }
            }
        }
        
        return new DadosPlanilha(
            arquivo.getOriginalFilename(),
            linhas.size(),
            cabecalhos,
            linhas,
            erros
        );
    }
    
    private void validarCabecalhos(List<String> cabecalhos, List<ErroProcessamento> erros) {
        for (String cabecalhoEsperado : CABECALHOS_ESPERADOS) {
            if (!cabecalhos.contains(cabecalhoEsperado)) {
                erros.add(new ErroProcessamento(
                    1, cabecalhoEsperado, "", 
                    "Cabeçalho obrigatório não encontrado: " + cabecalhoEsperado,
                    TipoErro.CAMPO_OBRIGATORIO
                ));
            }
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
    
    private LocalDate parseData(String dataStr) {
        for (DateTimeFormatter formatter : FORMATOS_DATA) {
            try {
                return LocalDate.parse(dataStr, formatter);
            } catch (DateTimeParseException e) {
                // Tentar próximo formato
            }
        }
        throw new IllegalArgumentException("Formato de data inválido: " + dataStr);
    }
    
    private BigDecimal parseValor(String valorStr) {
        // Remover caracteres não numéricos exceto vírgula, ponto e sinal negativo
        String valorLimpo = valorStr.replaceAll("[^\\d.,-]", "");
        
        // Substituir vírgula por ponto para decimal
        valorLimpo = valorLimpo.replace(",", ".");
        
        try {
            return new BigDecimal(valorLimpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de valor inválido: " + valorStr);
        }
    }
    
    private FormatoArquivo detectarFormato(MultipartFile arquivo) {
        String contentType = arquivo.getContentType();
        String nomeArquivo = arquivo.getOriginalFilename();
        
        if (contentType != null && TIPOS_MIME_EXCEL.contains(contentType)) {
            return FormatoArquivo.EXCEL_XLSX;
        }
        
        if (contentType != null && TIPOS_MIME_CSV.contains(contentType)) {
            return FormatoArquivo.CSV;
        }
        
        // Fallback para extensão do arquivo
        if (nomeArquivo != null) {
            if (nomeArquivo.toLowerCase().endsWith(".xlsx")) {
                return FormatoArquivo.EXCEL_XLSX;
            }
            if (nomeArquivo.toLowerCase().endsWith(".csv")) {
                return FormatoArquivo.CSV;
            }
        }
        
        return FormatoArquivo.DESCONHECIDO;
    }
    
    private double calcularSimilaridade(TransacaoImportada importada, TransacaoExistente existente) {
        double pontuacao = 0.0;
        double pesoTotal = 0.0;
        
        // Comparar data (peso 30%)
        if (importada.data().equals(existente.data())) {
            pontuacao += 0.3;
        }
        pesoTotal += 0.3;
        
        // Comparar valor (peso 40%)
        if (importada.valor().compareTo(existente.valor()) == 0) {
            pontuacao += 0.4;
        }
        pesoTotal += 0.4;
        
        // Comparar descrição (peso 20%)
        double similaridadeDescricao = calcularSimilaridadeTexto(
            importada.descricao(), existente.descricao()
        );
        pontuacao += similaridadeDescricao * 0.2;
        pesoTotal += 0.2;
        
        // Comparar categoria (peso 10%)
        if (importada.categoria().equalsIgnoreCase(existente.categoria())) {
            pontuacao += 0.1;
        }
        pesoTotal += 0.1;
        
        return pontuacao / pesoTotal;
    }
    
    private double calcularSimilaridadeTexto(String texto1, String texto2) {
        if (texto1 == null || texto2 == null) return 0.0;
        
        String t1 = texto1.toLowerCase().trim();
        String t2 = texto2.toLowerCase().trim();
        
        if (t1.equals(t2)) return 1.0;
        
        // Algoritmo simples de distância de Levenshtein normalizada
        int distancia = calcularDistanciaLevenshtein(t1, t2);
        int maxLength = Math.max(t1.length(), t2.length());
        
        return maxLength == 0 ? 1.0 : 1.0 - (double) distancia / maxLength;
    }
    
    private int calcularDistanciaLevenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private String determinarMotivoDeteccao(TransacaoImportada importada, TransacaoExistente existente) {
        List<String> motivos = new ArrayList<>();
        
        if (importada.data().equals(existente.data())) {
            motivos.add("mesma data");
        }
        
        if (importada.valor().compareTo(existente.valor()) == 0) {
            motivos.add("mesmo valor");
        }
        
        if (calcularSimilaridadeTexto(importada.descricao(), existente.descricao()) > 0.8) {
            motivos.add("descrição similar");
        }
        
        if (importada.categoria().equalsIgnoreCase(existente.categoria())) {
            motivos.add("mesma categoria");
        }
        
        return String.join(", ", motivos);
    }
}