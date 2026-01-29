package com.gestaofinanceira.domain.valueobjects;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for SenhaHash Value Object.
 * Tests universal properties that should hold for all valid inputs.
 */
@Label("Feature: gestao-financeira-domestica, SenhaHash Value Object Properties")
class SenhaHashPropertyTest {

    /**
     * Property: Password verification round-trip consistency
     * For any valid password, creating a SenhaHash and verifying the original password should always succeed
     * **Validates: Requirements 1.1, 1.2**
     */
    @Property(tries = 20)
    @Label("Property 1: Valid credentials authentication - Password verification round-trip")
    void passwordVerificationRoundTripShouldBeConsistent(@ForAll("validPasswords") String password) {
        // Act
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(password);
        
        // Assert
        assertThat(senhaHash.verificarSenha(password)).isTrue();
    }

    /**
     * Property: Invalid password rejection consistency
     * For any valid SenhaHash and different password, verification should always fail
     * **Validates: Requirements 1.1, 1.2**
     */
    @Property(tries = 20)
    @Label("Property 2: Invalid credentials rejection - Wrong password verification")
    void wrongPasswordVerificationShouldAlwaysFail(
            @ForAll("validPasswords") String correctPassword,
            @ForAll("validPasswords") String wrongPassword) {
        
        Assume.that(!correctPassword.equals(wrongPassword));
        
        // Arrange
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(correctPassword);
        
        // Act & Assert
        assertThat(senhaHash.verificarSenha(wrongPassword)).isFalse();
    }

    /**
     * Property: Hash uniqueness
     * For any valid password, creating multiple SenhaHash instances should produce different hashes
     */
    @Property(tries = 20)
    @Label("Property: Hash uniqueness for same password")
    void hashUniquenessForSamePassword(@ForAll("validPasswords") String password) {
        // Act
        SenhaHash hash1 = SenhaHash.criarDeSenhaTexto(password);
        SenhaHash hash2 = SenhaHash.criarDeSenhaTexto(password);
        
        // Assert
        assertThat(hash1.hash()).isNotEqualTo(hash2.hash());
        assertThat(hash1.salt()).isNotEqualTo(hash2.salt());
        
        // But both should verify the password correctly
        assertThat(hash1.verificarSenha(password)).isTrue();
        assertThat(hash2.verificarSenha(password)).isTrue();
    }

    /**
     * Property: Salt uniqueness
     * For any valid password, each SenhaHash creation should generate a unique salt
     */
    @Property(tries = 20)
    @Label("Property: Salt uniqueness")
    void saltShouldBeUnique(@ForAll("validPasswords") String password) {
        // Act
        SenhaHash hash1 = SenhaHash.criarDeSenhaTexto(password);
        SenhaHash hash2 = SenhaHash.criarDeSenhaTexto(password);
        
        // Assert
        assertThat(hash1.salt()).isNotEqualTo(hash2.salt());
        assertThat(hash1.salt()).isNotEmpty();
        assertThat(hash2.salt()).isNotEmpty();
    }

    /**
     * Property: Hash determinism with same salt
     * For any valid password and salt, the hash should be deterministic
     */
    @Property(tries = 20)
    @Label("Property: Hash determinism with same salt")
    void hashShouldBeDeterministicWithSameSalt(@ForAll("validPasswords") String password) {
        // Arrange
        SenhaHash originalHash = SenhaHash.criarDeSenhaTexto(password);
        
        // Act - Create new hash with same salt
        SenhaHash sameHash = new SenhaHash(originalHash.hash(), originalHash.salt());
        
        // Assert
        assertThat(sameHash).isEqualTo(originalHash);
        assertThat(sameHash.verificarSenha(password)).isTrue();
    }

    /**
     * Property: Invalid password strength rejection
     * For any password that doesn't meet strength requirements, creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Invalid password strength rejection")
    void weakPasswordsShouldBeRejected(@ForAll("weakPasswords") String weakPassword) {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto(weakPassword))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property: Password length validation
     * For any password shorter than 8 characters or longer than 128, creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Password length validation")
    void invalidLengthPasswordsShouldBeRejected(
            @ForAll @StringLength(min = 1, max = 7) String shortPassword,
            @ForAll @StringLength(min = 129, max = 150) String longPassword) {
        
        // Act & Assert - Short password
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto(shortPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha deve ter pelo menos 8 caracteres");
        
        // Act & Assert - Long password
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto(longPassword))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha não pode ter mais de 128 caracteres");
    }

    /**
     * Property: Null password rejection
     * Null passwords should always be rejected
     */
    @Property(tries = 20)
    @Label("Property: Null password rejection")
    void nullPasswordShouldBeRejected() {
        // Act & Assert
        assertThatThrownBy(() -> SenhaHash.criarDeSenhaTexto(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Senha não pode ser nula");
    }

    /**
     * Property: Hash and salt non-nullability
     * For any valid password, the resulting hash and salt should never be null or empty
     */
    @Property(tries = 20)
    @Label("Property: Hash and salt non-nullability")
    void hashAndSaltShouldNeverBeNullOrEmpty(@ForAll("validPasswords") String password) {
        // Act
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(password);
        
        // Assert
        assertThat(senhaHash.hash()).isNotNull();
        assertThat(senhaHash.salt()).isNotNull();
        assertThat(senhaHash.hash()).isNotEmpty();
        assertThat(senhaHash.salt()).isNotEmpty();
    }

    /**
     * Property: toString security
     * For any valid SenhaHash, toString should never expose sensitive data
     */
    @Property(tries = 20)
    @Label("Property: toString security")
    void toStringShouldNotExposeSensitiveData(@ForAll("validPasswords") String password) {
        // Act
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(password);
        String toString = senhaHash.toString();
        
        // Assert
        assertThat(toString).isEqualTo("SenhaHash{hash=*****, salt=*****}");
        assertThat(toString).doesNotContain(senhaHash.hash());
        assertThat(toString).doesNotContain(senhaHash.salt());
        assertThat(toString).doesNotContain(password);
    }

    // Generators for test data

    @Provide
    Arbitrary<String> validPasswords() {
        // Generate passwords that meet all strength requirements:
        // - At least 8 characters
        // - At most 128 characters
        // - Contains lowercase letter
        // - Contains uppercase letter
        // - Contains digit
        // - Contains special character (@$!%*?&)
        
        Arbitrary<String> lowercase = Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1)
            .ofMaxLength(3);
            
        Arbitrary<String> uppercase = Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(1)
            .ofMaxLength(3);
            
        Arbitrary<String> digits = Arbitraries.strings()
            .withCharRange('0', '9')
            .ofMinLength(1)
            .ofMaxLength(3);
            
        Arbitrary<String> specials = Arbitraries.strings()
            .withChars('@', '$', '!', '%', '*', '?', '&')
            .ofMinLength(1)
            .ofMaxLength(2);
            
        Arbitrary<String> filler = Arbitraries.strings()
            .withChars('a', 'b', 'c', 'A', 'B', 'C', '1', '2', '3', '@', '$')
            .ofMinLength(0)
            .ofMaxLength(20);

        return Combinators.combine(lowercase, uppercase, digits, specials, filler)
            .as((low, up, dig, spec, fill) -> {
                String password = low + up + dig + spec + fill;
                // Ensure it's within length limits
                if (password.length() > 128) {
                    password = password.substring(0, 128);
                }
                return password;
            })
            .filter(pwd -> pwd.length() >= 8 && pwd.length() <= 128);
    }

    @Provide
    Arbitrary<String> weakPasswords() {
        return Arbitraries.oneOf(
            // Too short
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(7),
            // No uppercase
            Arbitraries.strings().withChars('a', 'b', 'c', '1', '2', '@').ofMinLength(8).ofMaxLength(20),
            // No lowercase
            Arbitraries.strings().withChars('A', 'B', 'C', '1', '2', '@').ofMinLength(8).ofMaxLength(20),
            // No digits
            Arbitraries.strings().withChars('a', 'b', 'A', 'B', '@', '$').ofMinLength(8).ofMaxLength(20),
            // No special characters
            Arbitraries.strings().withChars('a', 'b', 'A', 'B', '1', '2').ofMinLength(8).ofMaxLength(20)
        );
    }
}
