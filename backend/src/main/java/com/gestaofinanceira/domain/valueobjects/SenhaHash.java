package com.gestaofinanceira.domain.valueobjects;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object para representar senhas com hash seguro.
 * Implementa hashing com salt usando SHA-256.
 */
public record SenhaHash(String hash, String salt) {
    
    private static final String ALGORITMO = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    // Padrão para validação de senha forte
    private static final Pattern SENHA_FORTE_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    public SenhaHash {
        Objects.requireNonNull(hash, "Hash da senha não pode ser nulo");
        Objects.requireNonNull(salt, "Salt da senha não pode ser nulo");
        
        if (hash.trim().isEmpty()) {
            throw new IllegalArgumentException("Hash da senha não pode estar vazio");
        }
        
        if (salt.trim().isEmpty()) {
            throw new IllegalArgumentException("Salt da senha não pode estar vazio");
        }
    }
    
    /**
     * Cria um SenhaHash a partir de uma senha em texto plano.
     * Gera automaticamente um salt aleatório.
     */
    public static SenhaHash criarDeSenhaTexto(String senhaTexto) {
        Objects.requireNonNull(senhaTexto, "Senha não pode ser nula");
        
        validarForcaSenha(senhaTexto);
        
        String salt = gerarSalt();
        String hash = calcularHash(senhaTexto, salt);
        
        return new SenhaHash(hash, salt);
    }
    
    /**
     * Verifica se uma senha em texto plano corresponde a este hash.
     */
    public boolean verificarSenha(String senhaTexto) {
        Objects.requireNonNull(senhaTexto, "Senha não pode ser nula");
        
        String hashCalculado = calcularHash(senhaTexto, this.salt);
        return MessageDigest.isEqual(
            this.hash.getBytes(),
            hashCalculado.getBytes()
        );
    }
    
    /**
     * Valida se a senha atende aos critérios de força.
     */
    private static void validarForcaSenha(String senha) {
        if (senha.length() < 8) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 8 caracteres");
        }
        
        if (senha.length() > 128) {
            throw new IllegalArgumentException("Senha não pode ter mais de 128 caracteres");
        }
        
        if (!SENHA_FORTE_PATTERN.matcher(senha).matches()) {
            throw new IllegalArgumentException(
                "Senha deve conter pelo menos: 1 letra minúscula, 1 maiúscula, 1 número e 1 caractere especial (@$!%*?&)"
            );
        }
    }
    
    /**
     * Gera um salt aleatório.
     */
    private static String gerarSalt() {
        byte[] saltBytes = new byte[32];
        RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
    
    /**
     * Calcula o hash SHA-256 da senha com salt.
     */
    private static String calcularHash(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            digest.update(salt.getBytes());
            byte[] hashBytes = digest.digest(senha.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo de hash não disponível: " + ALGORITMO, e);
        }
    }
    
    /**
     * Verifica se a senha precisa ser atualizada (rehash).
     * Útil para migração de algoritmos de hash.
     */
    public boolean precisaRehash() {
        // Por enquanto, sempre retorna false
        // Pode ser implementado para detectar hashes antigos
        return false;
    }
    
    /**
     * Retorna uma representação segura (sem expor o hash real).
     */
    @Override
    public String toString() {
        return "SenhaHash{hash=*****, salt=*****}";
    }
}