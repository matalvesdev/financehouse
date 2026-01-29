package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object para representar nomes de pessoas.
 * Garante que apenas nomes válidos sejam aceitos no sistema.
 */
public record Nome(String valor) {
    
    public Nome {
        Objects.requireNonNull(valor, "Nome não pode ser nulo");
        
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode estar vazio");
        }
        
        // Valida se não começa ou termina com espaço, hífen ou apostrofe ANTES de fazer trim
        if (valor.matches("^[\\s'-].*|.*[\\s'-]$")) {
            throw new IllegalArgumentException("Nome não pode começar ou terminar com espaço, hífen ou apostrofe");
        }
        
        valor = valor.trim();
        
        if (valor.length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }
        
        if (valor.length() > 100) {
            throw new IllegalArgumentException("Nome não pode ter mais de 100 caracteres");
        }
        
        // Valida se contém apenas letras, espaços, hífens e apostrofes
        if (!valor.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
            throw new IllegalArgumentException("Nome deve conter apenas letras, espaços, hífens e apostrofes");
        }
        
        // Valida se não tem espaços consecutivos
        if (valor.contains("  ")) {
            throw new IllegalArgumentException("Nome não pode conter espaços consecutivos");
        }
    }
    
    /**
     * Retorna o primeiro nome.
     */
    public String getPrimeiroNome() {
        String[] partes = valor.split("\\s+");
        return partes[0];
    }
    
    /**
     * Retorna o último nome (sobrenome).
     */
    public String getUltimoNome() {
        String[] partes = valor.split("\\s+");
        return partes.length > 1 ? partes[partes.length - 1] : partes[0];
    }
    
    /**
     * Retorna os nomes do meio (se houver).
     */
    public String getNomesDoMeio() {
        String[] partes = valor.split("\\s+");
        if (partes.length <= 2) {
            return "";
        }
        
        StringBuilder nomesDoMeio = new StringBuilder();
        for (int i = 1; i < partes.length - 1; i++) {
            if (i > 1) {
                nomesDoMeio.append(" ");
            }
            nomesDoMeio.append(partes[i]);
        }
        
        return nomesDoMeio.toString();
    }
    
    /**
     * Retorna o nome formatado com a primeira letra de cada palavra em maiúscula.
     */
    public String getFormatado() {
        String[] palavras = valor.toLowerCase().split("\\s+");
        StringBuilder nomeFormatado = new StringBuilder();
        
        for (int i = 0; i < palavras.length; i++) {
            if (i > 0) {
                nomeFormatado.append(" ");
            }
            
            String palavra = palavras[i];
            if (!palavra.isEmpty()) {
                // Trata preposições e artigos (de, da, do, dos, das, e, etc.)
                if (isPreposicaoOuArtigo(palavra) && i > 0 && i < palavras.length - 1) {
                    nomeFormatado.append(palavra.toLowerCase());
                } else {
                    nomeFormatado.append(Character.toUpperCase(palavra.charAt(0)))
                                 .append(palavra.substring(1).toLowerCase());
                }
            }
        }
        
        return nomeFormatado.toString();
    }
    
    /**
     * Retorna as iniciais do nome.
     */
    public String getIniciais() {
        String[] palavras = valor.split("\\s+");
        StringBuilder iniciais = new StringBuilder();
        
        for (String palavra : palavras) {
            if (!palavra.isEmpty() && !isPreposicaoOuArtigo(palavra)) {
                iniciais.append(Character.toUpperCase(palavra.charAt(0)));
            }
        }
        
        return iniciais.toString();
    }
    
    /**
     * Verifica se uma palavra é uma preposição ou artigo comum em nomes.
     */
    private boolean isPreposicaoOuArtigo(String palavra) {
        String palavraLower = palavra.toLowerCase();
        return palavraLower.equals("de") || palavraLower.equals("da") || 
               palavraLower.equals("do") || palavraLower.equals("dos") || 
               palavraLower.equals("das") || palavraLower.equals("e") ||
               palavraLower.equals("del") || palavraLower.equals("della") ||
               palavraLower.equals("von") || palavraLower.equals("van");
    }
    
    /**
     * Verifica se o nome é composto (tem mais de uma palavra).
     */
    public boolean ehComposto() {
        return valor.contains(" ");
    }
    
    /**
     * Retorna o número de palavras no nome.
     */
    public int getQuantidadePalavras() {
        return valor.split("\\s+").length;
    }
    
    @Override
    public String toString() {
        return getFormatado();
    }
}