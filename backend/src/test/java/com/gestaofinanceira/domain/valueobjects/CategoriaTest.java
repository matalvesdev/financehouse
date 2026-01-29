package com.gestaofinanceira.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Categoria Value Object Tests")
class CategoriaTest {

    @Test
    @DisplayName("Deve criar categoria válida")
    void deveCriarCategoriaValida() {
        // Arrange & Act
        Categoria categoria = new Categoria("ALIMENTACAO", TipoCategoria.DESPESA);
        
        // Assert
        assertThat(categoria.nome()).isEqualTo("ALIMENTACAO");
        assertThat(categoria.tipo()).isEqualTo(TipoCategoria.DESPESA);
    }

    @Test
    @DisplayName("Deve normalizar nome para uppercase")
    void deveNormalizarNomeParaUppercase() {
        // Arrange & Act
        Categoria categoria = new Categoria("alimentacao", TipoCategoria.DESPESA);
        
        // Assert
        assertThat(categoria.nome()).isEqualTo("ALIMENTACAO");
    }

    @Test
    @DisplayName("Deve remover espaços em branco")
    void deveRemoverEspacosEmBranco() {
        // Arrange & Act
        Categoria categoria = new Categoria("  ALIMENTACAO  ", TipoCategoria.DESPESA);
        
        // Assert
        assertThat(categoria.nome()).isEqualTo("ALIMENTACAO");
    }

    @Test
    @DisplayName("Deve rejeitar nome nulo")
    void deveRejeitarNomeNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria(null, TipoCategoria.DESPESA))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Nome da categoria não pode ser nulo");
    }

    @Test
    @DisplayName("Deve rejeitar tipo nulo")
    void deveRejeitarTipoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria("ALIMENTACAO", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Tipo da categoria não pode ser nulo");
    }

    @Test
    @DisplayName("Deve rejeitar nome vazio")
    void deveRejeitarNomeVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria("", TipoCategoria.DESPESA))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome da categoria não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar nome muito longo")
    void deveRejeitarNomeMuitoLongo() {
        // Arrange
        String nomeLongo = "A".repeat(51);
        
        // Act & Assert
        assertThatThrownBy(() -> new Categoria(nomeLongo, TipoCategoria.DESPESA))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome da categoria não pode ter mais de 50 caracteres");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "CATEGORIA_VALIDA",
        "CATEGORIA123",
        "CAT_123_TESTE",
        "A",
        "CATEGORIA_COM_NUMEROS_123"
    })
    @DisplayName("Deve aceitar nomes válidos")
    void deveAceitarNomesValidos(String nomeValido) {
        // Act & Assert
        assertThatCode(() -> new Categoria(nomeValido, TipoCategoria.DESPESA))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "categoria com espaços",
        "categoria-com-hifen",
        "categoria.com.ponto",
        "categoria@especial",
        "categoria#hash",
        "categoria%percent"
    })
    @DisplayName("Deve rejeitar nomes inválidos")
    void deveRejeitarNomesInvalidos(String nomeInvalido) {
        // Act & Assert
        assertThatThrownBy(() -> new Categoria(nomeInvalido, TipoCategoria.DESPESA))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome da categoria deve conter apenas letras, números e underscore");
    }

    @Test
    @DisplayName("Deve criar categoria personalizada de despesa")
    void deveCriarCategoriaPersonalizadaDeDespesa() {
        // Arrange & Act
        Categoria categoria = Categoria.despesaPersonalizada("MINHA_CATEGORIA");
        
        // Assert
        assertThat(categoria.nome()).isEqualTo("MINHA_CATEGORIA");
        assertThat(categoria.tipo()).isEqualTo(TipoCategoria.DESPESA);
        assertThat(categoria.ehPersonalizada()).isTrue();
    }

    @Test
    @DisplayName("Deve criar categoria personalizada de receita")
    void deveCriarCategoriaPersonalizadaDeReceita() {
        // Arrange & Act
        Categoria categoria = Categoria.receitaPersonalizada("MINHA_RECEITA");
        
        // Assert
        assertThat(categoria.nome()).isEqualTo("MINHA_RECEITA");
        assertThat(categoria.tipo()).isEqualTo(TipoCategoria.RECEITA);
        assertThat(categoria.ehPersonalizada()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar categorias predefinidas")
    void deveIdentificarCategoriasPredefinidas() {
        // Act & Assert
        assertThat(Categoria.ALIMENTACAO.ehPredefinida()).isTrue();
        assertThat(Categoria.SALARIO.ehPredefinida()).isTrue();
        assertThat(Categoria.despesaPersonalizada("CUSTOM").ehPredefinida()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar tipo de categoria")
    void deveIdentificarTipoDeCategoria() {
        // Act & Assert
        assertThat(Categoria.ALIMENTACAO.ehDespesa()).isTrue();
        assertThat(Categoria.ALIMENTACAO.ehReceita()).isFalse();
        assertThat(Categoria.SALARIO.ehReceita()).isTrue();
        assertThat(Categoria.SALARIO.ehDespesa()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar categorias predefinidas")
    void deveRetornarCategoriasPredefinidas() {
        // Act
        Set<Categoria> categorias = Categoria.getCategoriasPredefinidas();
        
        // Assert
        assertThat(categorias).isNotEmpty();
        assertThat(categorias).contains(Categoria.ALIMENTACAO, Categoria.SALARIO);
        
        // Verifica imutabilidade
        assertThatThrownBy(() -> categorias.add(Categoria.despesaPersonalizada("TESTE")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve retornar categorias de despesa predefinidas")
    void deveRetornarCategoriasDespesaPredefinidas() {
        // Act
        Set<Categoria> categorias = Categoria.getCategoriasDespesaPredefinidas();
        
        // Assert
        assertThat(categorias).isNotEmpty();
        assertThat(categorias).contains(Categoria.ALIMENTACAO, Categoria.TRANSPORTE);
        assertThat(categorias).doesNotContain(Categoria.SALARIO);
        assertThat(categorias).allMatch(Categoria::ehDespesa);
    }

    @Test
    @DisplayName("Deve retornar categorias de receita predefinidas")
    void deveRetornarCategoriasReceitaPredefinidas() {
        // Act
        Set<Categoria> categorias = Categoria.getCategoriasReceitaPredefinidas();
        
        // Assert
        assertThat(categorias).isNotEmpty();
        assertThat(categorias).contains(Categoria.SALARIO, Categoria.FREELANCE);
        assertThat(categorias).doesNotContain(Categoria.ALIMENTACAO);
        assertThat(categorias).allMatch(Categoria::ehReceita);
    }

    @Test
    @DisplayName("Deve buscar categoria predefinida por nome")
    void deveBuscarCategoriaPredefinidaPorNome() {
        // Act & Assert
        assertThat(Categoria.buscarPredefinida("ALIMENTACAO")).isEqualTo(Categoria.ALIMENTACAO);
        assertThat(Categoria.buscarPredefinida("alimentacao")).isEqualTo(Categoria.ALIMENTACAO);
        assertThat(Categoria.buscarPredefinida("  ALIMENTACAO  ")).isEqualTo(Categoria.ALIMENTACAO);
        assertThat(Categoria.buscarPredefinida("INEXISTENTE")).isNull();
    }

    @Test
    @DisplayName("Deve verificar se existe categoria predefinida")
    void deveVerificarSeExisteCategoriaPredefinida() {
        // Act & Assert
        assertThat(Categoria.existePredefinida("ALIMENTACAO")).isTrue();
        assertThat(Categoria.existePredefinida("alimentacao")).isTrue();
        assertThat(Categoria.existePredefinida("INEXISTENTE")).isFalse();
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        Categoria categoria = Categoria.ALIMENTACAO;
        
        // Act & Assert
        assertThat(categoria.toString()).isEqualTo("ALIMENTACAO");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode corretamente")
    void deveImplementarEqualsEHashCodeCorretamente() {
        // Arrange
        Categoria categoria1 = new Categoria("ALIMENTACAO", TipoCategoria.DESPESA);
        Categoria categoria2 = new Categoria("alimentacao", TipoCategoria.DESPESA);
        Categoria categoria3 = new Categoria("TRANSPORTE", TipoCategoria.DESPESA);
        Categoria categoria4 = new Categoria("ALIMENTACAO", TipoCategoria.RECEITA);
        
        // Act & Assert
        assertThat(categoria1).isEqualTo(categoria2);
        assertThat(categoria1).isNotEqualTo(categoria3);
        assertThat(categoria1).isNotEqualTo(categoria4);
        assertThat(categoria1.hashCode()).isEqualTo(categoria2.hashCode());
    }
}