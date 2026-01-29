package com.gestaofinanceira.domain.valueobjects;

/**
 * Enumeração dos status possíveis de um orçamento.
 */
public enum StatusOrcamento {
    ATIVO("Ativo"),
    PROXIMO_LIMITE("Próximo do Limite"),
    EXCEDIDO("Excedido"),
    ARQUIVADO("Arquivado");
    
    private final String descricao;
    
    StatusOrcamento(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se o orçamento está ativo.
     */
    public boolean ehAtivo() {
        return this == ATIVO || this == PROXIMO_LIMITE;
    }
    
    /**
     * Verifica se o orçamento foi excedido.
     */
    public boolean foiExcedido() {
        return this == EXCEDIDO;
    }
    
    /**
     * Verifica se o orçamento está próximo do limite.
     */
    public boolean estaProximoLimite() {
        return this == PROXIMO_LIMITE;
    }
    
    /**
     * Verifica se o orçamento está arquivado.
     */
    public boolean estaArquivado() {
        return this == ARQUIVADO;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}