package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um orçamento definido pelo usuário para uma categoria específica.
 * 
 * Responsabilidades:
 * - Controlar limite de gastos por categoria e período
 * - Rastrear gastos atuais em tempo real
 * - Calcular status do orçamento (ativo, próximo do limite, excedido)
 * - Validar regras de negócio para orçamentos
 */
public class Orcamento {
    
    private final OrcamentoId id;
    private final UsuarioId usuarioId;
    private final Categoria categoria;
    private Valor limite;
    private final PeriodoOrcamento periodo;
    private Valor gastoAtual;
    private StatusOrcamento status;
    private final LocalDate inicioPeriodo;
    private final LocalDate fimPeriodo;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    // Constante para definir quando o orçamento está próximo do limite (80%)
    private static final BigDecimal PERCENTUAL_PROXIMO_LIMITE = new BigDecimal("0.80");
    
    /**
     * Construtor para criação de novo orçamento.
     */
    public Orcamento(OrcamentoId id, UsuarioId usuarioId, Categoria categoria, 
                     Valor limite, PeriodoOrcamento periodo, LocalDate inicioPeriodo) {
        this.id = Objects.requireNonNull(id, "ID do orçamento não pode ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        this.limite = Objects.requireNonNull(limite, "Limite não pode ser nulo");
        this.periodo = Objects.requireNonNull(periodo, "Período não pode ser nulo");
        this.inicioPeriodo = Objects.requireNonNull(inicioPeriodo, "Início do período não pode ser nulo");
        this.fimPeriodo = periodo.calcularFimPeriodo(inicioPeriodo);
        this.gastoAtual = Valor.zero(limite.moeda());
        this.status = StatusOrcamento.ATIVO;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
        
        validarOrcamento();
    }
    
    /**
     * Construtor para reconstrução de orçamento existente (usado por repositórios).
     */
    public Orcamento(OrcamentoId id, UsuarioId usuarioId, Categoria categoria, Valor limite,
                     PeriodoOrcamento periodo, Valor gastoAtual, StatusOrcamento status,
                     LocalDate inicioPeriodo, LocalDate fimPeriodo, 
                     LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = Objects.requireNonNull(id, "ID do orçamento não pode ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        this.limite = Objects.requireNonNull(limite, "Limite não pode ser nulo");
        this.periodo = Objects.requireNonNull(periodo, "Período não pode ser nulo");
        this.gastoAtual = Objects.requireNonNull(gastoAtual, "Gasto atual não pode ser nulo");
        this.status = Objects.requireNonNull(status, "Status não pode ser nulo");
        this.inicioPeriodo = Objects.requireNonNull(inicioPeriodo, "Início do período não pode ser nulo");
        this.fimPeriodo = Objects.requireNonNull(fimPeriodo, "Fim do período não pode ser nulo");
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "Data de atualização não pode ser nula");
        
        validarOrcamento();
    }
    
    /**
     * Factory method para criar um novo orçamento.
     */
    public static Orcamento criar(UsuarioId usuarioId, Categoria categoria, 
                                  Valor limite, PeriodoOrcamento periodo, LocalDate inicioPeriodo) {
        return new Orcamento(OrcamentoId.gerar(), usuarioId, categoria, limite, periodo, inicioPeriodo);
    }
    
    /**
     * Valida as regras de negócio do orçamento.
     */
    private void validarOrcamento() {
        // Limite deve ser positivo
        if (!limite.ehPositivo()) {
            throw new IllegalArgumentException("Limite do orçamento deve ser positivo");
        }
        
        // Categoria deve ser de despesa
        if (!categoria.ehDespesa()) {
            throw new IllegalArgumentException("Orçamento só pode ser criado para categorias de despesa");
        }
        
        // Gasto atual não pode ser negativo
        if (gastoAtual.ehNegativo()) {
            throw new IllegalArgumentException("Gasto atual não pode ser negativo");
        }
        
        // Início do período não pode ser futuro
        if (inicioPeriodo.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Início do período não pode ser futuro");
        }
    }
    
    /**
     * Adiciona um gasto ao orçamento e atualiza o status.
     */
    public void adicionarGasto(Valor valorGasto) {
        Objects.requireNonNull(valorGasto, "Valor do gasto não pode ser nulo");
        
        if (!valorGasto.ehPositivo()) {
            throw new IllegalArgumentException("Valor do gasto deve ser positivo");
        }
        
        if (status.estaArquivado()) {
            throw new IllegalStateException("Não é possível adicionar gasto a orçamento arquivado");
        }
        
        this.gastoAtual = this.gastoAtual.somar(valorGasto);
        this.atualizadoEm = LocalDateTime.now();
        
        atualizarStatus();
    }
    
    /**
     * Remove um gasto do orçamento (quando transação é excluída/modificada).
     */
    public void removerGasto(Valor valorGasto) {
        Objects.requireNonNull(valorGasto, "Valor do gasto não pode ser nulo");
        
        if (!valorGasto.ehPositivo()) {
            throw new IllegalArgumentException("Valor do gasto deve ser positivo");
        }
        
        if (status.estaArquivado()) {
            throw new IllegalStateException("Não é possível remover gasto de orçamento arquivado");
        }
        
        Valor novoGasto = this.gastoAtual.subtrair(valorGasto);
        
        // Não permite gasto negativo
        if (novoGasto.ehNegativo()) {
            this.gastoAtual = Valor.zero(gastoAtual.moeda());
        } else {
            this.gastoAtual = novoGasto;
        }
        
        this.atualizadoEm = LocalDateTime.now();
        atualizarStatus();
    }
    
    /**
     * Atualiza o limite do orçamento.
     */
    public void atualizarLimite(Valor novoLimite) {
        Objects.requireNonNull(novoLimite, "Novo limite não pode ser nulo");
        
        if (!novoLimite.ehPositivo()) {
            throw new IllegalArgumentException("Limite do orçamento deve ser positivo");
        }
        
        if (status.estaArquivado()) {
            throw new IllegalStateException("Não é possível atualizar limite de orçamento arquivado");
        }
        
        this.limite = novoLimite;
        this.atualizadoEm = LocalDateTime.now();
        
        atualizarStatus();
    }
    
    /**
     * Atualiza o status do orçamento baseado no gasto atual.
     */
    private void atualizarStatus() {
        if (status.estaArquivado()) {
            return; // Não atualiza status de orçamento arquivado
        }
        
        if (excedeuLimite()) {
            this.status = StatusOrcamento.EXCEDIDO;
        } else if (estaProximoDoLimite()) {
            this.status = StatusOrcamento.PROXIMO_LIMITE;
        } else {
            this.status = StatusOrcamento.ATIVO;
        }
    }
    
    /**
     * Arquiva o orçamento (fim do período).
     */
    public void arquivar() {
        if (status.estaArquivado()) {
            throw new IllegalStateException("Orçamento já está arquivado");
        }
        
        this.status = StatusOrcamento.ARQUIVADO;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Verifica se o orçamento está próximo do limite (80% ou mais).
     */
    public boolean estaProximoDoLimite() {
        BigDecimal percentualGasto = calcularPercentualGasto();
        return percentualGasto.compareTo(PERCENTUAL_PROXIMO_LIMITE) >= 0 && 
               percentualGasto.compareTo(BigDecimal.ONE) < 0;
    }
    
    /**
     * Verifica se o orçamento excedeu o limite.
     */
    public boolean excedeuLimite() {
        return gastoAtual.ehMaiorQue(limite);
    }
    
    /**
     * Calcula o percentual gasto do orçamento.
     */
    public BigDecimal calcularPercentualGasto() {
        if (limite.ehZero()) {
            return BigDecimal.ZERO;
        }
        
        return gastoAtual.quantia()
            .divide(limite.quantia(), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula o valor restante do orçamento.
     */
    public Valor calcularValorRestante() {
        if (excedeuLimite()) {
            return Valor.zero(limite.moeda());
        }
        
        return limite.subtrair(gastoAtual);
    }
    
    /**
     * Calcula o valor excedido (se houver).
     */
    public Valor calcularValorExcedido() {
        if (!excedeuLimite()) {
            return Valor.zero(limite.moeda());
        }
        
        return gastoAtual.subtrair(limite);
    }
    
    /**
     * Verifica se uma data está dentro do período do orçamento.
     */
    public boolean contemData(LocalDate data) {
        return periodo.contemData(inicioPeriodo, data);
    }
    
    /**
     * Verifica se o período do orçamento já expirou.
     */
    public boolean periodoExpirou() {
        return LocalDate.now().isAfter(fimPeriodo);
    }
    
    /**
     * Verifica se o orçamento pode receber novos gastos.
     */
    public boolean podeReceberGastos() {
        return status.ehAtivo() && !periodoExpirou();
    }
    
    // Getters
    public OrcamentoId getId() {
        return id;
    }
    
    public UsuarioId getUsuarioId() {
        return usuarioId;
    }
    
    public Categoria getCategoria() {
        return categoria;
    }
    
    public Valor getLimite() {
        return limite;
    }
    
    public PeriodoOrcamento getPeriodo() {
        return periodo;
    }
    
    public Valor getGastoAtual() {
        return gastoAtual;
    }
    
    public StatusOrcamento getStatus() {
        return status;
    }
    
    public LocalDate getInicioPeriodo() {
        return inicioPeriodo;
    }
    
    public LocalDate getFimPeriodo() {
        return fimPeriodo;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orcamento orcamento = (Orcamento) o;
        return Objects.equals(id, orcamento.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Orcamento{id=%s, usuarioId=%s, categoria=%s, limite=%s, gastoAtual=%s, status=%s, periodo=%s}", 
            id, usuarioId, categoria, limite, gastoAtual, status, periodo);
    }
}