package com.gestaofinanceira.domain.valueobjects;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Enumeração dos períodos de orçamento suportados.
 */
public enum PeriodoOrcamento {
    MENSAL("Mensal", ChronoUnit.MONTHS, 1),
    TRIMESTRAL("Trimestral", ChronoUnit.MONTHS, 3),
    ANUAL("Anual", ChronoUnit.YEARS, 1);
    
    private final String descricao;
    private final ChronoUnit unidade;
    private final long quantidade;
    
    PeriodoOrcamento(String descricao, ChronoUnit unidade, long quantidade) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.quantidade = quantidade;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public ChronoUnit getUnidade() {
        return unidade;
    }
    
    public long getQuantidade() {
        return quantidade;
    }
    
    /**
     * Calcula a data de fim do período a partir de uma data de início.
     */
    public LocalDate calcularFimPeriodo(LocalDate inicio) {
        return inicio.plus(quantidade, unidade).minusDays(1);
    }
    
    /**
     * Verifica se uma data está dentro do período definido.
     */
    public boolean contemData(LocalDate inicio, LocalDate data) {
        LocalDate fim = calcularFimPeriodo(inicio);
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
    
    /**
     * Calcula o próximo período a partir de uma data de início.
     */
    public LocalDate calcularProximoPeriodo(LocalDate inicio) {
        return inicio.plus(quantidade, unidade);
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}