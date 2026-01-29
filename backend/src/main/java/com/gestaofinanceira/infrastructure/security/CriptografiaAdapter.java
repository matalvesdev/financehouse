package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementação do CriptografiaPort usando algoritmos seguros.
 * 
 * Utiliza:
 * - BCrypt para hashing de senhas (com salt automático)
 * - AES-256-GCM para criptografia simétrica de dados
 * - SecureRandom para geração de tokens e salts
 * - Validação robusta de força de senhas
 * 
 * Requirements: 10.1, 1.5
 */
@Component
public class CriptografiaAdapter implements CriptografiaPort {
    
    private static final Logger logger = LoggerFactory.getLogger(CriptografiaAdapter.class);
    
    // Configurações de criptografia
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int AES_KEY_LENGTH = 256; // bits
    
    // Configurações de senha
    private static final int BCRYPT_STRENGTH = 12; // Força do BCrypt
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    // Padrões para validação de senha
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    
    private final PasswordEncoder passwordEncoder;
    private final SecretKey encryptionKey;
    private final SecureRandom secureRandom;
    
    public CriptografiaAdapter(@Value("${app.encryption.key:}") String encryptionKeyBase64) {
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
        this.secureRandom = new SecureRandom();
        this.encryptionKey = initializeEncryptionKey(encryptionKeyBase64);
        
        logger.info("CriptografiaAdapter inicializado com BCrypt strength: {}", BCRYPT_STRENGTH);
    }
    
    @Override
    public String hashearSenha(String senhaTextoPlano) {
        if (senhaTextoPlano == null || senhaTextoPlano.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        
        try {
            String hash = passwordEncoder.encode(senhaTextoPlano);
            logger.debug("Senha hasheada com sucesso");
            return hash;
        } catch (Exception e) {
            logger.error("Erro ao hashear senha", e);
            throw new RuntimeException("Erro interno ao processar senha", e);
        }
    }
    
    @Override
    public boolean verificarSenha(String senhaTextoPlano, String hashArmazenado) {
        if (senhaTextoPlano == null || hashArmazenado == null) {
            return false;
        }
        
        try {
            boolean matches = passwordEncoder.matches(senhaTextoPlano, hashArmazenado);
            logger.debug("Verificação de senha: {}", matches ? "sucesso" : "falha");
            return matches;
        } catch (Exception e) {
            logger.error("Erro ao verificar senha", e);
            return false;
        }
    }
    
    @Override
    public String criptografarDados(String dadosTextoPlano) {
        if (dadosTextoPlano == null || dadosTextoPlano.isEmpty()) {
            throw new IllegalArgumentException("Dados não podem ser nulos ou vazios");
        }
        
        try {
            // Gerar IV aleatório para GCM
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Configurar cipher
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, gcmSpec);
            
            // Criptografar dados
            byte[] dadosBytes = dadosTextoPlano.getBytes(StandardCharsets.UTF_8);
            byte[] dadosCriptografados = cipher.doFinal(dadosBytes);
            
            // Combinar IV + dados criptografados
            byte[] resultado = new byte[GCM_IV_LENGTH + dadosCriptografados.length];
            System.arraycopy(iv, 0, resultado, 0, GCM_IV_LENGTH);
            System.arraycopy(dadosCriptografados, 0, resultado, GCM_IV_LENGTH, dadosCriptografados.length);
            
            String resultadoBase64 = Base64.getEncoder().encodeToString(resultado);
            logger.debug("Dados criptografados com sucesso (tamanho: {} bytes)", resultado.length);
            
            return resultadoBase64;
            
        } catch (Exception e) {
            logger.error("Erro ao criptografar dados", e);
            throw new RuntimeException("Erro interno ao criptografar dados", e);
        }
    }
    
    @Override
    public String descriptografarDados(String dadosCriptografados) {
        if (dadosCriptografados == null || dadosCriptografados.isEmpty()) {
            throw new IllegalArgumentException("Dados criptografados não podem ser nulos ou vazios");
        }
        
        try {
            // Decodificar Base64
            byte[] dadosBytes = Base64.getDecoder().decode(dadosCriptografados);
            
            if (dadosBytes.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Dados criptografados inválidos (muito pequenos)");
            }
            
            // Extrair IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(dadosBytes, 0, iv, 0, GCM_IV_LENGTH);
            
            // Extrair dados criptografados
            byte[] dadosCriptografadosBytes = new byte[dadosBytes.length - GCM_IV_LENGTH];
            System.arraycopy(dadosBytes, GCM_IV_LENGTH, dadosCriptografadosBytes, 0, dadosCriptografadosBytes.length);
            
            // Configurar cipher para descriptografia
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmSpec);
            
            // Descriptografar dados
            byte[] dadosDescriptografados = cipher.doFinal(dadosCriptografadosBytes);
            String resultado = new String(dadosDescriptografados, StandardCharsets.UTF_8);
            
            logger.debug("Dados descriptografados com sucesso");
            return resultado;
            
        } catch (Exception e) {
            logger.error("Erro ao descriptografar dados", e);
            throw new RuntimeException("Erro interno ao descriptografar dados", e);
        }
    }
    
    @Override
    public String gerarTokenSeguro(int tamanho) {
        if (tamanho <= 0 || tamanho > 1024) {
            throw new IllegalArgumentException("Tamanho do token deve estar entre 1 e 1024 bytes");
        }
        
        byte[] tokenBytes = new byte[tamanho];
        secureRandom.nextBytes(tokenBytes);
        
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        logger.debug("Token seguro gerado (tamanho: {} bytes)", tamanho);
        
        return token;
    }
    
    @Override
    public String gerarSalt() {
        // Gerar salt de 32 bytes (256 bits)
        byte[] saltBytes = new byte[32];
        secureRandom.nextBytes(saltBytes);
        
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        logger.debug("Salt gerado");
        
        return salt;
    }
    
    @Override
    public ResultadoValidacaoSenha validarForcaSenha(String senha) {
        if (senha == null) {
            return new ResultadoValidacaoSenha(false, 0, NivelForca.MUITO_FRACA, 
                List.of("Senha não pode ser nula"));
        }
        
        List<String> sugestoes = new ArrayList<>();
        int pontuacao = 0;
        
        // Verificar comprimento
        if (senha.length() < MIN_PASSWORD_LENGTH) {
            sugestoes.add("Senha deve ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres");
        } else if (senha.length() >= MIN_PASSWORD_LENGTH) {
            pontuacao += 10;
            if (senha.length() >= 12) {
                pontuacao += 10; // Bonus para senhas mais longas
            }
        }
        
        if (senha.length() > MAX_PASSWORD_LENGTH) {
            sugestoes.add("Senha não pode ter mais que " + MAX_PASSWORD_LENGTH + " caracteres");
            return new ResultadoValidacaoSenha(false, 0, NivelForca.MUITO_FRACA, sugestoes);
        }
        
        // Verificar letras minúsculas
        if (LOWERCASE_PATTERN.matcher(senha).find()) {
            pontuacao += 15;
        } else {
            sugestoes.add("Adicione pelo menos uma letra minúscula");
        }
        
        // Verificar letras maiúsculas
        if (UPPERCASE_PATTERN.matcher(senha).find()) {
            pontuacao += 15;
        } else {
            sugestoes.add("Adicione pelo menos uma letra maiúscula");
        }
        
        // Verificar dígitos
        if (DIGIT_PATTERN.matcher(senha).find()) {
            pontuacao += 15;
        } else {
            sugestoes.add("Adicione pelo menos um número");
        }
        
        // Verificar caracteres especiais
        if (SPECIAL_CHAR_PATTERN.matcher(senha).find()) {
            pontuacao += 20;
        } else {
            sugestoes.add("Adicione pelo menos um caractere especial (!@#$%^&*...)");
        }
        
        // Verificar padrões comuns (penalizar)
        if (senha.toLowerCase().contains("password") || 
            senha.toLowerCase().contains("123456") ||
            senha.toLowerCase().contains("qwerty")) {
            pontuacao -= 20;
            sugestoes.add("Evite padrões comuns como 'password', '123456', 'qwerty'");
        }
        
        // Verificar repetições excessivas
        if (temRepeticoesExcessivas(senha)) {
            pontuacao -= 10;
            sugestoes.add("Evite repetir o mesmo caractere muitas vezes seguidas");
        }
        
        // Determinar nível de força
        NivelForca nivel;
        if (pontuacao < 30) {
            nivel = NivelForca.MUITO_FRACA;
        } else if (pontuacao < 50) {
            nivel = NivelForca.FRACA;
        } else if (pontuacao < 70) {
            nivel = NivelForca.MEDIA;
        } else if (pontuacao < 85) {
            nivel = NivelForca.FORTE;
        } else {
            nivel = NivelForca.MUITO_FORTE;
        }
        
        boolean valida = pontuacao >= 50 && sugestoes.isEmpty();
        
        return new ResultadoValidacaoSenha(valida, pontuacao, nivel, sugestoes);
    }
    
    /**
     * Inicializa a chave de criptografia.
     */
    private SecretKey initializeEncryptionKey(String encryptionKeyBase64) {
        try {
            if (encryptionKeyBase64 != null && !encryptionKeyBase64.isEmpty()) {
                // Usar chave fornecida
                byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
                if (keyBytes.length != AES_KEY_LENGTH / 8) {
                    throw new IllegalArgumentException("Chave de criptografia deve ter 256 bits (32 bytes)");
                }
                return new SecretKeySpec(keyBytes, AES_ALGORITHM);
            } else {
                // Gerar chave aleatória (apenas para desenvolvimento)
                logger.warn("Chave de criptografia não fornecida, gerando chave aleatória (NÃO USE EM PRODUÇÃO)");
                KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
                keyGenerator.init(AES_KEY_LENGTH);
                return keyGenerator.generateKey();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo AES não disponível", e);
        }
    }
    
    /**
     * Verifica se a senha tem repetições excessivas de caracteres.
     */
    private boolean temRepeticoesExcessivas(String senha) {
        int maxRepeticoes = 0;
        int repeticoesAtuais = 1;
        
        for (int i = 1; i < senha.length(); i++) {
            if (senha.charAt(i) == senha.charAt(i - 1)) {
                repeticoesAtuais++;
            } else {
                maxRepeticoes = Math.max(maxRepeticoes, repeticoesAtuais);
                repeticoesAtuais = 1;
            }
        }
        
        maxRepeticoes = Math.max(maxRepeticoes, repeticoesAtuais);
        return maxRepeticoes > 3; // Mais de 3 caracteres iguais seguidos
    }
}