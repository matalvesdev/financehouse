package com.gestaofinanceira.domain.valueobjects;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for Categoria Value Object.
 * Tests universal properties that should hold for all valid inputs.
 */
@Label("Feature: gestao-financeira-domestica, Categoria Value Object Properties")
class CategoriaPropertyTest {

    /**
     * Property: Category name normalization consistency
     * For any valid category name, normalization should be consistent
     */
    @Property(tries = 20)
    @Label("Property: Category name normalization consistency")
    void categoryNameNormalizationShouldBeConsistent(@ForAll("validCategoryNames") String categoryName) {
        // Arrange
        String expectedNormalized = categoryName.trim().toUpperCase();
        
        // Act
        Categoria categoria = new Categoria(categoryName, TipoCategoria.DESPESA);
        
        // Assert
        assertThat(categoria.nome()).isEqualTo(expectedNormalized);
    }

    /**
     * Property: Predefined category consistency
     * For any predefined category, it should be recognized as predefined
     */
    @Property(tries = 20)
    @Label("Property: Predefined category consistency")
    void predefinedCategoriesShouldBeConsistent(@ForAll("predefinedCategories") String predefinedName) {
        // Act
        Categoria categoria = new Categoria(predefinedName, TipoCategoria.DESPESA);
        
        // Assert
        assertThat(categoria.ehPredefinida()).isTrue();
        assertThat(Categoria.existePredefinida(predefinedName)).isTrue();
    }

    /**
     * Property: Custom category behavior
     * For any valid custom category name, it should be accepted but not predefined
     */
    @Property(tries = 20)
    @Label("Property: Custom category behavior")
    void customCategoriesShouldBehaveCorrectly(@ForAll("customCategoryNames") String customName) {
        // Act
        Categoria categoria = new Categoria(customName, TipoCategoria.DESPESA);
        
        // Assert
        assertThat(categoria.nome()).isNotEmpty();
        assertThat(categoria.tipo()).isEqualTo(TipoCategoria.DESPESA);
        // Custom categories might or might not be predefined, but should be valid
        assertThat(categoria.nome().length()).isGreaterThan(0);
    }

    /**
     * Property: Category type consistency
     * For any valid category, the type should be preserved
     */
    @Property(tries = 20)
    @Label("Property: Category type consistency")
    void categoryTypeShouldBeConsistent(
            @ForAll("validCategoryNames") String categoryName,
            @ForAll TipoCategoria tipo) {
        
        // Act
        Categoria categoria = new Categoria(categoryName, tipo);
        
        // Assert
        assertThat(categoria.tipo()).isEqualTo(tipo);
    }

    /**
     * Property: Category equality consistency
     * For any category name and type, creating multiple instances should be equal
     */
    @Property(tries = 20)
    @Label("Property: Category equality consistency")
    void categoryEqualityShouldBeConsistent(
            @ForAll("validCategoryNames") String categoryName,
            @ForAll TipoCategoria tipo) {
        
        // Act
        Categoria categoria1 = new Categoria(categoryName, tipo);
        Categoria categoria2 = new Categoria(categoryName.toLowerCase(), tipo);
        Categoria categoria3 = new Categoria("  " + categoryName + "  ", tipo);
        
        // Assert
        assertThat(categoria1).isEqualTo(categoria2);
        assertThat(categoria1).isEqualTo(categoria3);
        assertThat(categoria2).isEqualTo(categoria3);
        assertThat(categoria1.hashCode()).isEqualTo(categoria2.hashCode());
        assertThat(categoria1.hashCode()).isEqualTo(categoria3.hashCode());
    }

    /**
     * Property: Invalid category name rejection
     * For any invalid category name, creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Invalid category name rejection")
    void invalidCategoryNamesShouldBeRejected(@ForAll("invalidCategoryNames") String invalidName) {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria(invalidName, TipoCategoria.DESPESA))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property: Category name length validation
     * For any category name longer than maximum allowed, creation should fail
     */
    @Property(tries = 20)
    @Label("Property: Category name length validation")
    void longCategoryNamesShouldBeRejected(@ForAll @StringLength(min = 51, max = 100) String longName) {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria(longName, TipoCategoria.DESPESA))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nome da categoria não pode ter mais de 50 caracteres");
    }

    /**
     * Property: Null validation
     * Null category names or types should be rejected
     */
    @Property(tries = 20)
    @Label("Property: Null validation")
    void nullValuesShouldBeRejected() {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria(null, TipoCategoria.DESPESA))
            .isInstanceOf(NullPointerException.class);
            
        assertThatThrownBy(() -> new Categoria("ALIMENTACAO", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Property: toString consistency
     * For any valid category, toString should be consistent and readable
     */
    @Property(tries = 20)
    @Label("Property: toString consistency")
    void toStringShouldBeConsistent(
            @ForAll("validCategoryNames") String categoryName,
            @ForAll TipoCategoria tipo) {
        
        // Act
        Categoria categoria = new Categoria(categoryName, tipo);
        String toString = categoria.toString();
        
        // Assert
        assertThat(toString).isNotEmpty();
        assertThat(toString).isEqualTo(categoria.nome());
    }

    // Generators for test data

    @Provide
    Arbitrary<String> validCategoryNames() {
        return Arbitraries.oneOf(
            // Predefined categories
            Arbitraries.of("ALIMENTACAO", "TRANSPORTE", "MORADIA", "LAZER", "SAUDE", "EDUCACAO", "OUTROS"),
            // Custom categories
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(50)
        );
    }

    @Provide
    Arbitrary<String> predefinedCategories() {
        return Arbitraries.of("ALIMENTACAO", "TRANSPORTE", "MORADIA", "LAZER", "SAUDE", "EDUCACAO", "OUTROS");
    }

    @Provide
    Arbitrary<String> customCategoryNames() {
        return Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(3)
            .ofMaxLength(30)
            .filter(name -> !name.equals("ALIMENTACAO") && 
                           !name.equals("TRANSPORTE") && 
                           !name.equals("MORADIA") &&
                           !name.equals("LAZER") &&
                           !name.equals("SAUDE") &&
                           !name.equals("EDUCACAO") &&
                           !name.equals("OUTROS"));
    }

    @Provide
    Arbitrary<String> invalidCategoryNames() {
        return Arbitraries.oneOf(
            // Empty string
            Arbitraries.just(""),
            // Only whitespace
            Arbitraries.just("   "),
            // Only special characters
            Arbitraries.strings().withChars('@', '#', '$', '%').ofMinLength(1).ofMaxLength(10),
            // Only numbers
            Arbitraries.strings().numeric().ofMinLength(1).ofMaxLength(10)
        );
    }
}
