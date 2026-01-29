package com.gestaofinanceira.domain.valueobjects;

/**
 * Enumeração dos tipos de categoria de transação.
 */
public enum TipoCategoria {
    RECEITA("Receita"),
    DESPESA("Despesa");
    
    private final String descricao;
    
    TipoCategoria(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}