package com.gestaofinanceira.domain.valueobjects;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for Nome Value Object.
 * Tests universal properties that should hold for all valid inputs.
 */
@Label("Feature: gestao-financeira-domestica, Nome Value Object Properties")
class NomePropertyTest {

    /**
     * Property: Name normalization consistency
     * For any valid name string, the stored value should be trimmed but not formatted
     */
    @Property(tries = 20)
    @Label("Property: Name normalization consistency")
    void nameNormalizationShouldBeConsistent(@ForAll("validNames") String nameString) {
        // Arrange
        String expectedNormalized = nameString.trim();
        
        // Act
        Nome nome = new Nome(nameString);
        
        // Assert
        assertThat(nome.valor()).isEqualTo(expectedNormalized);
    }

    /**
     * Property: Name part extraction consistency
     * For any valid name, part extraction should be consistent
     */
    @Property(tries = 20)
    @Label("Property: Name part extraction consistency")
    void namePartExtractionShouldBeConsistent(@ForAll("validNames") String nameString) {
        // Act
        Nome nome = new Nome(nameString);
        
        // Assert
        String[] parts = nome.valor().split("\\s+");
        
        if (parts.length >= 1) {
            assertThat(nome.getPrimeiroNome()).isEqualTo(parts[0]);
        }
        
        if (parts.length >= 2) {
            assertThat(nome.getUltimoNome()).isEqualTo(parts[parts.length - 1]);
        } else {
            assertThat(nome.getUltimoNome()).isEqualTo(parts[0]);
        }
        
        if (parts.length > 2) {
            StringBuilder expectedMiddle = new StringBuilder();
            for (int i = 1; i < parts.length - 1; i++) {
                if (i > 1) expectedMiddle.append(" ");
                expectedMiddle.append(parts[i]);
            }
            assertThat(nome.getNomesDoMeio()).isEqualTo(expectedMiddle.toString());
        } else {
            assertThat(nome.getNomesDoMeio()).isEmpty();
        }
    }

    /**
     * Property: Initials generation consistency
     * For any valid name, initials should be generated correctly
     */
    @Property(tries = 20)
    @Label("Property: Initials generation consistency")
    void initialsGenerationShouldBeConsistent(@ForAll("validNames") String nameString) {
        // Act
        Nome nome = new Nome(nameString);
        String initials = nome.getIniciais();
        
        // Assert
        assertThat(initials).isNotEmpty();
        
        String[] parts = nome.valor().split("\\s+");
        StringBuilder expectedInitials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty() && !isPreposicaoOuArtigo(part)) {
                expectedInitials.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        
        assertThat(initials).isEqualTo(expectedInitials.toString());
    }
    
    // Helper method to check if a word is a preposition or article
    private boolean isPreposicaoOuArtigo(String palavra) {
        String palavraLower = palavra.toLowerCase();
        return palavraLower.equals("de") || palavraLower.equals("da") || 
               palavraLower.equals("do") || palavraLower.equals("dos") || 
               palavraLower.equals("das") || palavraLower.equals("e") ||
               palavraLower.equals("del") || palavraLower.equals("della") ||
               palavraLower.equals("von") || palavraLower.equals("van");
    }

    /**
     * Property: Name equality consistency
     * For any valid name string, creating multiple instances should be equal regardless of case
     */
    @Property(tries = 20)
    @Label("Property: Name equality consistency")
    void nameEqualityShouldBeConsistent(@ForAll("validNames") String nameString) {
        // Arrange - Only test case variations, not whitespace variations since Nome rejects leading/trailing spaces
        String variation1 = nameString.toLowerCase();
        String variation2 = nameString.toUpperCase();
        
        // Act
        Nome nome1 = new Nome(nameString);
        Nome nome2 = new Nome(variation1);
        Nome nome3 = new Nome(variation2);
        
        // Assert
        assertThat(nome1).isEqualTo(nome2);
        assertThat(nome1).isEqualTo(nome3);
        assertThat(nome1.hashCode()).isEqualTo(nome2.hashCode());
        assertThat(nome1.hashCode()).isEqualTo(nome3.hashCode());
    }

    /**
     * Property: Name validation consistency
     * For any valid name, it should meet all validation criteria
     */
    @Property(tries = 20)
    @Label("Property: Name validation consistency")
    void nameValidationShouldBeConsistent(@ForAll("validNames") String nameString) {
        // Act
        Nome nome = new Nome(nameString);
        
        // Assert
        assertThat(nome.valor()).isNotEmpty();
        assertThat(nome.valor().length()).isGreaterThanOrEqualTo(2);
        assertThat(nome.valor().length()).isLessThanOrEqualTo(100);
        assertThat(nome.valor()).matches("^[a-zA-ZÀ-ÿ\\s'-]+$"); // Letters, spaces, hyphens, apostrophes
    }

    /**
     * Property: Invalid name rejection
     * For any invalid name string, creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Invalid name rejection")
    void invalidNamesShouldBeRejected(@ForAll("invalidNames") String invalidName) {
        // Act & Assert
        assertThatThrownBy(() -> new Nome(invalidName))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property: Name length validation
     * For any name longer than 100 characters or shorter than 2, creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Name length validation")
    void invalidLengthNamesShouldBeRejected(
            @ForAll("shortNames") String shortName,
            @ForAll @StringLength(min = 101, max = 150) String longName) {
        
        // Act & Assert - Short name
        assertThatThrownBy(() -> new Nome(shortName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nome deve ter pelo menos 2 caracteres");
        
        // Act & Assert - Long name
        assertThatThrownBy(() -> new Nome(longName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nome não pode ter mais de 100 caracteres");
    }

    /**
     * Property: Null name rejection
     * Null names should always be rejected
     */
    @Property(tries = 20)
    @Label("Property: Null name rejection")
    void nullNameShouldBeRejected() {
        // Act & Assert
        assertThatThrownBy(() -> new Nome(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Nome não pode ser nulo");
    }

    /**
     * Property: toString consistency
     * For any valid name, toString should return the normalized value
     */
    @Property(tries = 20)
    @Label("Property: toString consistency")
    void toStringShouldBeConsistent(@ForAll("validNames") String nameString) {
        // Act
        Nome nome = new Nome(nameString);
        String toString = nome.toString();
        
        // Assert
        assertThat(toString).isEqualTo(nome.getFormatado());
    }

    // Helper method to normalize names as expected by the Nome class
    private String normalizeExpectedName(String name) {
        if (name == null) return null;
        
        // Trim and normalize spaces
        String normalized = name.trim().replaceAll("\\s+", " ");
        
        // Capitalize each word
        String[] words = normalized.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }

    // Generators for test data

    @Provide
    Arbitrary<String> validNames() {
        Arbitrary<String> firstName = Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(2)
            .ofMaxLength(15);
            
        Arbitrary<String> lastName = Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(2)
            .ofMaxLength(15);
            
        Arbitrary<String> middleName = Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(2)
            .ofMaxLength(10);

        return Arbitraries.oneOf(
            // Single name
            firstName,
            // First + Last
            Combinators.combine(firstName, lastName)
                .as((first, last) -> first + " " + last),
            // First + Middle + Last
            Combinators.combine(firstName, middleName, lastName)
                .as((first, middle, last) -> first + " " + middle + " " + last),
            // Names with accents
            Arbitraries.of("José", "María", "João", "André", "Luís", "Ângela", "Mônica", "Cláudia"),
            // Compound names
            Arbitraries.of("Ana Paula", "João Pedro", "Maria Clara", "José Carlos")
        );
    }

    @Provide
    Arbitrary<String> shortNames() {
        // Generate single character names that won't be empty after trim
        return Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofLength(1);
    }

    @Provide
    Arbitrary<String> invalidNames() {
        return Arbitraries.oneOf(
            // Empty string
            Arbitraries.just(""),
            // Only whitespace
            Arbitraries.just("   "),
            // Contains numbers
            Arbitraries.strings().withChars('A', 'B', '1', '2').ofMinLength(3).ofMaxLength(10),
            // Contains special characters
            Arbitraries.strings().withChars('A', 'B', '@', '#').ofMinLength(3).ofMaxLength(10),
            // Only numbers
            Arbitraries.strings().numeric().ofMinLength(2).ofMaxLength(10)
        );
    }
}
