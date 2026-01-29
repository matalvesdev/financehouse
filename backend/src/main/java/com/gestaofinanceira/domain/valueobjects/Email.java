package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object para representar um endereço de email válido.
 * Garante que apenas emails com formato válido sejam aceitos no sistema.
 */
public record Email(String valor) {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public Email {
        Objects.requireNonNull(valor, "Email não pode ser nulo");
        valor = valor.trim().toLowerCase();
        
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Email não pode estar vazio");
        }
        
        if (valor.length() > 255) {
            throw new IllegalArgumentException("Email não pode ter mais de 255 caracteres");
        }
        
        if (!isValidEmail(valor)) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
    }
    
    /**
     * Valida se o formato do email está correto.
     */
    private static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Retorna o domínio do email.
     */
    public String getDominio() {
        int atIndex = valor.indexOf('@');
        return valor.substring(atIndex + 1);
    }
    
    /**
     * Retorna a parte local do email (antes do @).
     */
    public String getParteLocal() {
        int atIndex = valor.indexOf('@');
        return valor.substring(0, atIndex);
    }
    
    /**
     * Verifica se o email pertence a um domínio específico.
     */
    public boolean pertenceAoDominio(String dominio) {
        return getDominio().equalsIgnoreCase(dominio);
    }
    
    @Override
    public String toString() {
        return valor;
    }
}