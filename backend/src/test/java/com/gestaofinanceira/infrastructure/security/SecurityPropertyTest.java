package com.gestaofinanceira.infrastructure.security;

import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de propriedade para funcionalidades de segurança.
 * 
 * Valida propriedades universais de:
 * - Criptografia de dados sensíveis
 * - Hashing de senhas
 * - Geração de tokens seguros
 * - Validação de força de senhas
 * 
 * Requirements: 10.1, 1.5
 */
class SecurityPropertyTest {
    
    private static final String ENCRYPTION_KEY = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA="; // 32 bytes base64 encoded
    
    private CriptografiaPort createCriptografiaPort() {
        return new CriptografiaAdapter(ENCRYPTION_KEY);
    }
    
    /**
     * Property 17: Data encryption consistency
     * For any valid string data, encrypting and then decrypting should return the original data
     * **Validates: Requirements 10.1**
     */
    @Property(tries = 20)
    @Label("Feature: gestao-financeira-domestica, Property 17: Data encryption consistency")
    void encryptionDecryptionRoundTripShouldBeConsistent(@ForAll("validStrings") String originalData) {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        String encryptedData = criptografiaPort.criptografarDados(originalData);
        String decryptedData = criptografiaPort.descriptografarDados(encryptedData);
        
        // Assert
        assertThat(decryptedData).isEqualTo(originalData);
        assertThat(encryptedData).isNotEqualTo(originalData); // Dados devem ser diferentes quando criptografados
        assertThat(encryptedData).isNotEmpty();
    }
    
    /**
     * Property: Password hashing consistency
     * For any valid password, hashing and then verifying should return true
     */
    @Property(tries = 20)
    @Label("Property: Password hashing consistency")
    void passwordHashingVerificationShouldBeConsistent(@ForAll("validPasswords") String password) {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        String hashedPassword = criptografiaPort.hashearSenha(password);
        boolean isValid = criptografiaPort.verificarSenha(password, hashedPassword);
        
        // Assert
        assertThat(isValid).isTrue();
        assertThat(hashedPassword).isNotEqualTo(password); // Hash deve ser diferente da senha original
        assertThat(hashedPassword).isNotEmpty();
    }
    
    /**
     * Property: Password hash uniqueness
     * For any valid password, multiple hashes should be different (due to salt)
     */
    @Property(tries = 20)
    @Label("Property: Password hash uniqueness")
    void passwordHashesShouldBeUnique(@ForAll("validPasswords") String password) {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        String hash1 = criptografiaPort.hashearSenha(password);
        String hash2 = criptografiaPort.hashearSenha(password);
        
        // Assert
        assertThat(hash1).isNotEqualTo(hash2); // BCrypt usa salt aleatório
        
        // Ambos os hashes devem ser válidos para a mesma senha
        assertThat(criptografiaPort.verificarSenha(password, hash1)).isTrue();
        assertThat(criptografiaPort.verificarSenha(password, hash2)).isTrue();
    }
    
    /**
     * Property: Wrong password rejection
     * For any two different passwords, verification should fail
     */
    @Property(tries = 20)
    @Label("Property: Wrong password rejection")
    void wrongPasswordShouldBeRejected(@ForAll("validPasswords") String correctPassword,
                                      @ForAll("validPasswords") String wrongPassword) {
        Assume.that(!correctPassword.equals(wrongPassword));
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Arrange
        String hashedPassword = criptografiaPort.hashearSenha(correctPassword);
        
        // Act
        boolean isValid = criptografiaPort.verificarSenha(wrongPassword, hashedPassword);
        
        // Assert
        assertThat(isValid).isFalse();
    }
    
    /**
     * Property: Secure token uniqueness
     * For any valid token size, generated tokens should be unique
     */
    @Property(tries = 20)
    @Label("Property: Secure token uniqueness")
    void secureTokensShouldBeUnique(@ForAll("tokenSizes") int tokenSize) {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        String token1 = criptografiaPort.gerarTokenSeguro(tokenSize);
        String token2 = criptografiaPort.gerarTokenSeguro(tokenSize);
        
        // Assert
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).isNotEmpty();
        assertThat(token2).isNotEmpty();
    }
    
    /**
     * Property: Salt uniqueness
     * Generated salts should always be unique
     */
    @Property(tries = 20)
    @Label("Property: Salt uniqueness")
    void saltsShouldBeUnique() {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        String salt1 = criptografiaPort.gerarSalt();
        String salt2 = criptografiaPort.gerarSalt();
        
        // Assert
        assertThat(salt1).isNotEqualTo(salt2);
        assertThat(salt1).isNotEmpty();
        assertThat(salt2).isNotEmpty();
    }
    
    /**
     * Property: Password strength validation consistency
     * For any password, validation should be deterministic
     */
    @Property(tries = 20)
    @Label("Property: Password strength validation consistency")
    void passwordStrengthValidationShouldBeConsistent(@ForAll("anyPasswords") String password) {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado1 = criptografiaPort.validarForcaSenha(password);
        CriptografiaPort.ResultadoValidacaoSenha resultado2 = criptografiaPort.validarForcaSenha(password);
        
        // Assert
        assertThat(resultado1.valida()).isEqualTo(resultado2.valida());
        assertThat(resultado1.pontuacao()).isEqualTo(resultado2.pontuacao());
        assertThat(resultado1.nivel()).isEqualTo(resultado2.nivel());
        assertThat(resultado1.sugestoesMelhoria()).isEqualTo(resultado2.sugestoesMelhoria());
    }
    
    /**
     * Property: Strong passwords should be valid
     * Passwords with all required characteristics should be considered strong
     */
    @Property(tries = 20)
    @Label("Property: Strong passwords should be valid")
    void strongPasswordsShouldBeValid(@ForAll("strongPasswords") String strongPassword) {
        CriptografiaPort criptografiaPort = createCriptografiaPort();
        
        // Act
        CriptografiaPort.ResultadoValidacaoSenha resultado = criptografiaPort.validarForcaSenha(strongPassword);
        
        // Assert
        assertThat(resultado.nivel()).isIn(
            CriptografiaPort.NivelForca.FORTE, 
            CriptografiaPort.NivelForca.MUITO_FORTE
        );
        assertThat(resultado.pontuacao()).isGreaterThan(50);
    }
    
    // Geradores de dados para os testes
    
    @Provide
    Arbitrary<String> validStrings() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars(' ', '.', ',', '!', '?', '-', '_')
            .ofMinLength(1)
            .ofMaxLength(1000);
    }
    
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('!', '@', '#', '$', '%', '^', '&', '*')
            .ofMinLength(8)
            .ofMaxLength(50);
    }
    
    @Provide
    Arbitrary<String> anyPasswords() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('!', '@', '#', '$', '%', '^', '&', '*', ' ')
            .ofMinLength(0)
            .ofMaxLength(200);
    }
    
    @Provide
    Arbitrary<String> strongPasswords() {
        return Combinators.combine(
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(6),
            Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(3).ofMaxLength(6),
            Arbitraries.strings().withCharRange('0', '9').ofMinLength(2).ofMaxLength(4),
            Arbitraries.strings().withChars('!', '@', '#', '$', '%').ofMinLength(2).ofMaxLength(4)
        ).as((lower, upper, digits, special) -> lower + upper + digits + special);
    }
    
    @Provide
    Arbitrary<Integer> tokenSizes() {
        return Arbitraries.integers().between(1, 256);
    }
}
