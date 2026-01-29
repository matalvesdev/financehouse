package com.gestaofinanceira.domain.valueobjects;

/**
 * Enumeração dos tipos de meta financeira.
 */
public enum TipoMeta {
    RESERVA_EMERGENCIA("Reserva de Emergência"),
    EMERGENCIA("Emergência"), // Alias para compatibilidade
    FERIAS("Férias"),
    VIAGEM("Viagem"),
    COMPRA("Compra"),
    INVESTIMENTO("Investimento"),
    APOSENTADORIA("Aposentadoria"),
    EDUCACAO("Educação"),
    CASA_PROPRIA("Casa Própria"),
    VEICULO("Veículo"),
    OUTROS("Outros");
    
    private final String descricao;
    
    TipoMeta(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Verifica se é uma meta de longo prazo (mais de 1 ano típico).
     */
    public boolean ehLongoPrazo() {
        return this == APOSENTADORIA || this == CASA_PROPRIA || this == EDUCACAO;
    }
    
    /**
     * Verifica se é uma meta de emergência.
     */
    public boolean ehEmergencia() {
        return this == RESERVA_EMERGENCIA || this == EMERGENCIA;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}