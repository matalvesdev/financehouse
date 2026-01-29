package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa uma transação financeira (receita ou despesa).
 * 
 * Responsabilidades:
 * - Manter dados da transação (valor, descrição, categoria, data)
 * - Validar regras de negócio para transações
 * - Controlar estado ativo/inativo da transação
 * - Calcular impacto no saldo e orçamentos
 */
public class Transacao {
    
    private final TransacaoId id;
    private final UsuarioId usuarioId;
    private Valor valor;
    private Descricao descricao;
    private Categoria categoria;
    private final LocalDate data;
    private final TipoTransacao tipo;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private boolean ativa;
    
    /**
     * Construtor para criação de nova transação.
     */
    public Transacao(TransacaoId id, UsuarioId usuarioId, Valor valor, Descricao descricao,
                     Categoria categoria, LocalDate data, TipoTransacao tipo) {
        this.id = Objects.requireNonNull(id, "ID da transação não pode ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        this.valor = Objects.requireNonNull(valor, "Valor não pode ser nulo");
        this.descricao = Objects.requireNonNull(descricao, "Descrição não pode ser nula");
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        this.data = Objects.requireNonNull(data, "Data não pode ser nula");
        this.tipo = Objects.requireNonNull(tipo, "Tipo não pode ser nulo");
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
        this.ativa = true;
        
        validarTransacao();
    }
    
    /**
     * Construtor para reconstrução de transação existente (usado por repositórios).
     */
    public Transacao(TransacaoId id, UsuarioId usuarioId, Valor valor, Descricao descricao,
                     Categoria categoria, LocalDate data, TipoTransacao tipo,
                     LocalDateTime criadoEm, LocalDateTime atualizadoEm, boolean ativa) {
        this.id = Objects.requireNonNull(id, "ID da transação não pode ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        this.valor = Objects.requireNonNull(valor, "Valor não pode ser nulo");
        this.descricao = Objects.requireNonNull(descricao, "Descrição não pode ser nula");
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        this.data = Objects.requireNonNull(data, "Data não pode ser nula");
        this.tipo = Objects.requireNonNull(tipo, "Tipo não pode ser nulo");
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "Data de atualização não pode ser nula");
        this.ativa = ativa;
        
        validarTransacao();
    }
    
    /**
     * Factory method para criar uma nova transação.
     */
    public static Transacao criar(UsuarioId usuarioId, Valor valor, Descricao descricao,
                                  Categoria categoria, LocalDate data, TipoTransacao tipo) {
        return new Transacao(TransacaoId.gerar(), usuarioId, valor, descricao, categoria, data, tipo);
    }
    
    /**
     * Valida as regras de negócio da transação.
     */
    private void validarTransacao() {
        // Valor deve ser positivo
        if (!valor.ehPositivo()) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
        
        // Data não pode ser futura
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da transação não pode ser futura");
        }
        
        // Categoria deve ser compatível com o tipo de transação
        if (tipo.ehReceita() && categoria.ehDespesa()) {
            throw new IllegalArgumentException("Categoria de despesa não pode ser usada em receita");
        }
        
        if (tipo.ehDespesa() && categoria.ehReceita()) {
            throw new IllegalArgumentException("Categoria de receita não pode ser usada em despesa");
        }
    }
    
    /**
     * Atualiza o valor da transação.
     */
    public void atualizarValor(Valor novoValor) {
        Objects.requireNonNull(novoValor, "Novo valor não pode ser nulo");
        
        if (!ativa) {
            throw new IllegalStateException("Não é possível atualizar transação inativa");
        }
        
        if (!novoValor.ehPositivo()) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
        
        this.valor = novoValor;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Atualiza a descrição da transação.
     */
    public void atualizarDescricao(Descricao novaDescricao) {
        Objects.requireNonNull(novaDescricao, "Nova descrição não pode ser nula");
        
        if (!ativa) {
            throw new IllegalStateException("Não é possível atualizar transação inativa");
        }
        
        this.descricao = novaDescricao;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Atualiza a categoria da transação.
     */
    public void atualizarCategoria(Categoria novaCategoria) {
        Objects.requireNonNull(novaCategoria, "Nova categoria não pode ser nula");
        
        if (!ativa) {
            throw new IllegalStateException("Não é possível atualizar transação inativa");
        }
        
        // Validar compatibilidade com tipo
        if (tipo.ehReceita() && novaCategoria.ehDespesa()) {
            throw new IllegalArgumentException("Categoria de despesa não pode ser usada em receita");
        }
        
        if (tipo.ehDespesa() && novaCategoria.ehReceita()) {
            throw new IllegalArgumentException("Categoria de receita não pode ser usada em despesa");
        }
        
        this.categoria = novaCategoria;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Desativa a transação (soft delete).
     */
    public void desativar() {
        if (!ativa) {
            throw new IllegalStateException("Transação já está inativa");
        }
        
        this.ativa = false;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Reativa a transação.
     */
    public void reativar() {
        if (ativa) {
            throw new IllegalStateException("Transação já está ativa");
        }
        
        this.ativa = true;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Verifica se esta transação afeta orçamentos.
     * Apenas despesas afetam orçamentos.
     */
    public boolean afetaOrcamento() {
        return ativa && tipo.ehDespesa();
    }
    
    /**
     * Retorna o valor com sinal correto para cálculo de saldo.
     * Receitas são positivas, despesas são negativas.
     */
    public Valor getValorComSinal() {
        return tipo.ehReceita() ? valor : valor.negar();
    }
    
    /**
     * Verifica se a transação pertence a um período específico.
     */
    public boolean pertenceAoPeriodo(LocalDate inicio, LocalDate fim) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
    
    /**
     * Verifica se a transação foi criada recentemente (últimos 7 dias).
     */
    public boolean ehRecente() {
        return criadoEm.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * Verifica se a transação foi modificada após a criação.
     */
    public boolean foiModificada() {
        return !criadoEm.equals(atualizadoEm);
    }
    
    // Getters
    public TransacaoId getId() {
        return id;
    }
    
    public UsuarioId getUsuarioId() {
        return usuarioId;
    }
    
    public Valor getValor() {
        return valor;
    }
    
    public Descricao getDescricao() {
        return descricao;
    }
    
    public Categoria getCategoria() {
        return categoria;
    }
    
    public LocalDate getData() {
        return data;
    }
    
    public TipoTransacao getTipo() {
        return tipo;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public boolean isAtiva() {
        return ativa;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transacao transacao = (Transacao) o;
        return Objects.equals(id, transacao.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Transacao{id=%s, usuarioId=%s, valor=%s, descricao=%s, categoria=%s, tipo=%s, data=%s, ativa=%s}", 
            id, usuarioId, valor, descricao, categoria, tipo, data, ativa);
    }
}