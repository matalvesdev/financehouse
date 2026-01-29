package com.gestaofinanceira.domain.valueobjects;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.LowerChars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for Email Value Object.
 * Tests universal properties that should hold for all valid inputs.
 */
@Label("Feature: gestao-financeira-domestica, Email Value Object Properties")
class EmailPropertyTest {

    /**
     * Property: Email normalization consistency
     * For any valid email string, creating an Email should normalize it to lowercase and trim whitespace
     */
    @Property(tries = 20)
    @Label("Property: Email normalization consistency")
    void emailNormalizationShouldBeConsistent(@ForAll("validEmailStrings") String emailString) {
        // Arrange
        String originalEmail = emailString;
        String expectedNormalized = emailString.trim().toLowerCase();
        
        // Act
        Email email = new Email(originalEmail);
        
        // Assert
        assertThat(email.valor()).isEqualTo(expectedNormalized);
    }

    /**
     * Property: Email domain extraction consistency
     * For any valid email, the domain should be the part after '@'
     */
    @Property(tries = 20)
    @Label("Property: Email domain extraction consistency")
    void domainExtractionShouldBeConsistent(@ForAll("validEmailStrings") String emailString) {
        // Arrange
        Email email = new Email(emailString);
        String normalizedEmail = emailString.trim().toLowerCase();
        int atIndex = normalizedEmail.indexOf('@');
        String expectedDomain = normalizedEmail.substring(atIndex + 1);
        
        // Act
        String actualDomain = email.getDominio();
        
        // Assert
        assertThat(actualDomain).isEqualTo(expectedDomain);
    }

    /**
     * Property: Email local part extraction consistency
     * For any valid email, the local part should be the part before '@'
     */
    @Property(tries = 20)
    @Label("Property: Email local part extraction consistency")
    void localPartExtractionShouldBeConsistent(@ForAll("validEmailStrings") String emailString) {
        // Arrange
        Email email = new Email(emailString);
        String normalizedEmail = emailString.trim().toLowerCase();
        int atIndex = normalizedEmail.indexOf('@');
        String expectedLocalPart = normalizedEmail.substring(0, atIndex);
        
        // Act
        String actualLocalPart = email.getParteLocal();
        
        // Assert
        assertThat(actualLocalPart).isEqualTo(expectedLocalPart);
    }

    /**
     * Property: Domain membership verification consistency
     * For any valid email, pertenceAoDominio should return true for its own domain
     */
    @Property(tries = 20)
    @Label("Property: Domain membership verification consistency")
    void domainMembershipShouldBeConsistent(@ForAll("validEmailStrings") String emailString) {
        // Arrange
        Email email = new Email(emailString);
        String domain = email.getDominio();
        
        // Act & Assert
        assertThat(email.pertenceAoDominio(domain)).isTrue();
        assertThat(email.pertenceAoDominio(domain.toUpperCase())).isTrue();
        assertThat(email.pertenceAoDominio(domain.toLowerCase())).isTrue();
    }

    /**
     * Property: Email equality consistency
     * For any valid email string, creating two Email objects should be equal regardless of case and whitespace
     */
    @Property(tries = 20)
    @Label("Property: Email equality consistency")
    void emailEqualityShouldBeConsistent(@ForAll("validEmailStrings") String emailString) {
        // Arrange
        String variation1 = emailString.toUpperCase();
        String variation2 = "  " + emailString.toLowerCase() + "  ";
        
        // Act
        Email email1 = new Email(emailString);
        Email email2 = new Email(variation1);
        Email email3 = new Email(variation2);
        
        // Assert
        assertThat(email1).isEqualTo(email2);
        assertThat(email1).isEqualTo(email3);
        assertThat(email2).isEqualTo(email3);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        assertThat(email1.hashCode()).isEqualTo(email3.hashCode());
    }

    /**
     * Property: Invalid email rejection consistency
     * For any string that doesn't match email pattern, Email creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Invalid email rejection consistency")
    void invalidEmailsShouldBeRejected(@ForAll("invalidEmailStrings") String invalidEmail) {
        // Act & Assert
        assertThatThrownBy(() -> new Email(invalidEmail))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property: Email length validation
     * For any email longer than 255 characters, Email creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Email length validation")
    void longEmailsShouldBeRejected(@ForAll @StringLength(min = 230, max = 250) String longString) {
        // Arrange - Create an email that will exceed 255 characters
        // longString (230-250) + "@example.com" (12) = 242-262 characters
        String longEmail = longString + "@example.com";
        
        // Only test if it actually exceeds 255 characters
        Assume.that(longEmail.length() > 255);
        
        // Act & Assert
        assertThatThrownBy(() -> new Email(longEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ter mais de 255 caracteres");
    }

    // Generators for test data

    @Provide
    Arbitrary<String> validEmailStrings() {
        Arbitrary<String> localPart = Combinators.combine(
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(10),
            Arbitraries.oneOf(
                Arbitraries.just(""),
                Arbitraries.strings().withCharRange('0', '9').ofMinLength(1).ofMaxLength(3),
                Arbitraries.strings().withChars('.', '_', '+', '-').ofMinLength(1).ofMaxLength(2)
            )
        ).as((base, suffix) -> base + suffix);

        Arbitrary<String> domain = Combinators.combine(
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(2).ofMaxLength(10),
            Arbitraries.oneOf(
                Arbitraries.just("com"),
                Arbitraries.just("org"),
                Arbitraries.just("net"),
                Arbitraries.just("edu"),
                Arbitraries.just("co.uk"),
                Arbitraries.just("com.br")
            )
        ).as((name, tld) -> name + "." + tld);

        return Combinators.combine(localPart, domain)
            .as((local, dom) -> local + "@" + dom);
    }

    @Provide
    Arbitrary<String> invalidEmailStrings() {
        return Arbitraries.oneOf(
            // Missing @
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20),
            // Missing local part
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> "@" + s + ".com"),
            // Missing domain
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> s + "@"),
            // Missing TLD
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> s + "@domain"),
            // Double dots
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> s + "..test@domain.com"),
            // Spaces
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> s + " test@domain.com"),
            // Empty string
            Arbitraries.just(""),
            // Only whitespace
            Arbitraries.just("   ")
        );
    }
}
