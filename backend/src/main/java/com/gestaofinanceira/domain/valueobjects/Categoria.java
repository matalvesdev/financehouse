package com.gestaofinanceira.domain.valueobjects;

import java.util.Objects;
import java.util.Set;

/**
 * Value Object para representar categorias de transações financeiras.
 * Suporta categorias predefinidas e personalizadas.
 */
public record Categoria(String nome, TipoCategoria tipo) {
    
    // Categorias predefinidas para despesas
    public static final Categoria ALIMENTACAO = new Categoria("ALIMENTACAO", TipoCategoria.DESPESA);
    public static final Categoria TRANSPORTE = new Categoria("TRANSPORTE", TipoCategoria.DESPESA);
    public static final Categoria MORADIA = new Categoria("MORADIA", TipoCategoria.DESPESA);
    public static final Categoria LAZER = new Categoria("LAZER", TipoCategoria.DESPESA);
    public static final Categoria SAUDE = new Categoria("SAUDE", TipoCategoria.DESPESA);
    public static final Categoria EDUCACAO = new Categoria("EDUCACAO", TipoCategoria.DESPESA);
    public static final Categoria VESTUARIO = new Categoria("VESTUARIO", TipoCategoria.DESPESA);
    public static final Categoria SERVICOS = new Categoria("SERVICOS", TipoCategoria.DESPESA);
    public static final Categoria IMPOSTOS = new Categoria("IMPOSTOS", TipoCategoria.DESPESA);
    public static final Categoria OUTROS_GASTOS = new Categoria("OUTROS_GASTOS", TipoCategoria.DESPESA);
    
    // Categorias predefinidas para receitas
    public static final Categoria SALARIO = new Categoria("SALARIO", TipoCategoria.RECEITA);
    public static final Categoria FREELANCE = new Categoria("FREELANCE", TipoCategoria.RECEITA);
    public static final Categoria INVESTIMENTOS = new Categoria("INVESTIMENTOS", TipoCategoria.RECEITA);
    public static final Categoria VENDAS = new Categoria("VENDAS", TipoCategoria.RECEITA);
    public static final Categoria OUTROS_GANHOS = new Categoria("OUTROS_GANHOS", TipoCategoria.RECEITA);
    
    // Conjunto de categorias predefinidas
    private static final Set<Categoria> CATEGORIAS_PREDEFINIDAS = Set.of(
        ALIMENTACAO, TRANSPORTE, MORADIA, LAZER, SAUDE, EDUCACAO, 
        VESTUARIO, SERVICOS, IMPOSTOS, OUTROS_GASTOS,
        SALARIO, FREELANCE, INVESTIMENTOS, VENDAS, OUTROS_GANHOS
    );
    
    public Categoria {
        Objects.requireNonNull(nome, "Nome da categoria não pode ser nulo");
        Objects.requireNonNull(tipo, "Tipo da categoria não pode ser nulo");
        
        nome = nome.trim().toUpperCase();
        
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria não pode estar vazio");
        }
        
        if (nome.length() > 50) {
            throw new IllegalArgumentException("Nome da categoria não pode ter mais de 50 caracteres");
        }
        
        // Valida caracteres permitidos (letras, números, underscore)
        if (!nome.matches("^[A-Z0-9_]+$")) {
            throw new IllegalArgumentException("Nome da categoria deve conter apenas letras, números e underscore");
        }
    }
    
    /**
     * Cria uma categoria personalizada de despesa.
     */
    public static Categoria despesaPersonalizada(String nome) {
        return new Categoria(nome, TipoCategoria.DESPESA);
    }
    
    /**
     * Cria uma categoria personalizada de receita.
     */
    public static Categoria receitaPersonalizada(String nome) {
        return new Categoria(nome, TipoCategoria.RECEITA);
    }
    
    /**
     * Verifica se esta categoria é predefinida pelo sistema.
     */
    public boolean ehPredefinida() {
        return CATEGORIAS_PREDEFINIDAS.contains(this);
    }
    
    /**
     * Verifica se esta categoria é personalizada pelo usuário.
     */
    public boolean ehPersonalizada() {
        return !ehPredefinida();
    }
    
    /**
     * Verifica se esta categoria é de despesa.
     */
    public boolean ehDespesa() {
        return tipo == TipoCategoria.DESPESA;
    }
    
    /**
     * Verifica se esta categoria é de receita.
     */
    public boolean ehReceita() {
        return tipo == TipoCategoria.RECEITA;
    }
    
    /**
     * Retorna todas as categorias predefinidas.
     */
    public static Set<Categoria> getCategoriasPredefinidas() {
        return Set.copyOf(CATEGORIAS_PREDEFINIDAS);
    }
    
    /**
     * Retorna todas as categorias predefinidas de despesa.
     */
    public static Set<Categoria> getCategoriasDespesaPredefinidas() {
        return CATEGORIAS_PREDEFINIDAS.stream()
            .filter(Categoria::ehDespesa)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Retorna todas as categorias predefinidas de receita.
     */
    public static Set<Categoria> getCategoriasReceitaPredefinidas() {
        return CATEGORIAS_PREDEFINIDAS.stream()
            .filter(Categoria::ehReceita)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Busca uma categoria predefinida pelo nome.
     */
    public static Categoria buscarPredefinida(String nome) {
        String nomeNormalizado = nome.trim().toUpperCase();
        return CATEGORIAS_PREDEFINIDAS.stream()
            .filter(cat -> cat.nome.equals(nomeNormalizado))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Verifica se existe uma categoria predefinida com o nome especificado.
     */
    public static boolean existePredefinida(String nome) {
        return buscarPredefinida(nome) != null;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}