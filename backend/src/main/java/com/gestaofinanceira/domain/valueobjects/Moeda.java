package com.gestaofinanceira.domain.valueobjects;

/**
 * Enumeração das moedas suportadas pelo sistema.
 */
public enum Moeda {
    BRL("R$", "Real Brasileiro"),
    USD("$", "Dólar Americano"),
    EUR("€", "Euro");
    
    private final String simbolo;
    private final String nome;
    
    Moeda(String simbolo, String nome) {
        this.simbolo = simbolo;
        this.nome = nome;
    }
    
    public String getSimbolo() {
        return simbolo;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getCodigo() {
        return name();
    }
    
    public static Moeda fromCodigo(String codigo) {
        return valueOf(codigo);
    }
    
    @Override
    public String toString() {
        return name();
    }
}