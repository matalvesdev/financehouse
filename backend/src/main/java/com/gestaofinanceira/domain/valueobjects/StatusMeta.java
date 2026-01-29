package com.gestaofinanceira.domain.valueobjects;

/**
 * Enumeração dos status possíveis de uma meta financeira.
 */
public enum StatusMeta {
    ATIVA("Ativa"),
    PAUSADA("Pausada"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada"),
    VENCIDA("Vencida");
    
    private final String descricao;
    
    StatusMeta(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se a meta está ativa.
     */
    public boolean ehAtiva() {
        return this == ATIVA;
    }
    
    /**
     * Verifica se a meta foi concluída.
     */
    public boolean foiConcluida() {
        return this == CONCLUIDA;
    }
    
    /**
     * Verifica se a meta está pausada.
     */
    public boolean estaPausada() {
        return this == PAUSADA;
    }
    
    /**
     * Verifica se a meta foi cancelada.
     */
    public boolean foiCancelada() {
        return this == CANCELADA;
    }
    
    /**
     * Verifica se a meta está vencida.
     */
    public boolean estaVencida() {
        return this == VENCIDA;
    }
    
    /**
     * Verifica se a meta pode receber contribuições.
     */
    public boolean podeReceberContribuicoes() {
        return this == ATIVA;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}