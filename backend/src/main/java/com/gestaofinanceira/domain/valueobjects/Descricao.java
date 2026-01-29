package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object para representar descrições de transações e outros elementos.
 */
public record Descricao(String valor) {
    
    public Descricao {
        Objects.requireNonNull(valor, "Descrição não pode ser nula");
        valor = valor.trim();
        
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode estar vazia");
        }
        
        if (valor.length() > 255) {
            throw new IllegalArgumentException("Descrição não pode ter mais de 255 caracteres");
        }
    }
    
    /**
     * Verifica se a descrição contém uma palavra-chave específica.
     */
    public boolean contem(String palavraChave) {
        Objects.requireNonNull(palavraChave, "Palavra-chave não pode ser nula");
        return valor.toLowerCase().contains(palavraChave.toLowerCase());
    }
    
    /**
     * Verifica se a descrição começa com um texto específico.
     */
    public boolean comecaCom(String prefixo) {
        Objects.requireNonNull(prefixo, "Prefixo não pode ser nulo");
        return valor.toLowerCase().startsWith(prefixo.toLowerCase());
    }
    
    /**
     * Retorna uma versão resumida da descrição.
     */
    public String resumo(int maxCaracteres) {
        if (maxCaracteres <= 0) {
            throw new IllegalArgumentException("Número máximo de caracteres deve ser positivo");
        }
        
        if (valor.length() <= maxCaracteres) {
            return valor;
        }
        
        return valor.substring(0, maxCaracteres - 3) + "...";
    }
    
    /**
     * Retorna a descrição formatada (primeira letra maiúscula).
     */
    public String formatada() {
        if (valor.isEmpty()) {
            return valor;
        }
        
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1).toLowerCase();
    }
    
    @Override
    public String toString() {
        return valor;
    }
}