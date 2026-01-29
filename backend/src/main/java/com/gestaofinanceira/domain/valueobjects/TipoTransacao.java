package com.gestaofinanceira.domain.valueobjects;

/**
 * Enumeração dos tipos de transação financeira.
 */
public enum TipoTransacao {
    RECEITA("Receita", 1),
    DESPESA("Despesa", -1);
    
    private final String descricao;
    private final int multiplicador;
    
    TipoTransacao(String descricao, int multiplicador) {
        this.descricao = descricao;
        this.multiplicador = multiplicador;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Retorna o multiplicador para cálculo de saldo.
     * RECEITA = +1, DESPESA = -1
     */
    public int getMultiplicador() {
        return multiplicador;
    }
    
    /**
     * Verifica se é uma receita.
     */
    public boolean ehReceita() {
        return this == RECEITA;
    }
    
    /**
     * Verifica se é uma despesa.
     */
    public boolean ehDespesa() {
        return this == DESPESA;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}