package com.gestaofinanceira.domain.entities;

import com.gestaofinanceira.domain.valueobjects.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidade que representa uma meta financeira definida pelo usuário.
 * 
 * Responsabilidades:
 * - Controlar progresso em direção a um objetivo financeiro
 * - Calcular percentual de conclusão e estimativas
 * - Validar regras de negócio para metas
 * - Gerenciar status da meta (ativa, concluída, pausada, etc.)
 */
public class MetaFinanceira {
    
    private final MetaId id;
    private final UsuarioId usuarioId;
    private Nome nome;
    private Valor valorAlvo;
    private Valor valorAtual;
    private LocalDate prazo;
    private final TipoMeta tipo;
    private StatusMeta status;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    /**
     * Construtor para criação de nova meta financeira.
     */
    public MetaFinanceira(MetaId id, UsuarioId usuarioId, Nome nome, Valor valorAlvo,
                          LocalDate prazo, TipoMeta tipo) {
        this.id = Objects.requireNonNull(id, "ID da meta não pode ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        this.nome = Objects.requireNonNull(nome, "Nome da meta não pode ser nulo");
        this.valorAlvo = Objects.requireNonNull(valorAlvo, "Valor alvo não pode ser nulo");
        this.prazo = Objects.requireNonNull(prazo, "Prazo não pode ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "Tipo da meta não pode ser nulo");
        this.valorAtual = Valor.zero(valorAlvo.moeda());
        this.status = StatusMeta.ATIVA;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
        
        validarMeta();
    }
    
    /**
     * Construtor para reconstrução de meta existente (usado por repositórios).
     */
    public MetaFinanceira(MetaId id, UsuarioId usuarioId, Nome nome, Valor valorAlvo,
                          Valor valorAtual, LocalDate prazo, TipoMeta tipo, StatusMeta status,
                          LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = Objects.requireNonNull(id, "ID da meta não pode ser nulo");
        this.usuarioId = Objects.requireNonNull(usuarioId, "ID do usuário não pode ser nulo");
        this.nome = Objects.requireNonNull(nome, "Nome da meta não pode ser nulo");
        this.valorAlvo = Objects.requireNonNull(valorAlvo, "Valor alvo não pode ser nulo");
        this.valorAtual = Objects.requireNonNull(valorAtual, "Valor atual não pode ser nulo");
        this.prazo = Objects.requireNonNull(prazo, "Prazo não pode ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "Tipo da meta não pode ser nulo");
        this.status = Objects.requireNonNull(status, "Status da meta não pode ser nulo");
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "Data de atualização não pode ser nula");
        
        validarMeta();
    }
    
    /**
     * Factory method para criar uma nova meta financeira.
     */
    public static MetaFinanceira criar(UsuarioId usuarioId, Nome nome, Valor valorAlvo,
                                       LocalDate prazo, TipoMeta tipo) {
        return new MetaFinanceira(MetaId.gerar(), usuarioId, nome, valorAlvo, prazo, tipo);
    }
    
    /**
     * Valida as regras de negócio da meta financeira.
     */
    private void validarMeta() {
        // Valor alvo deve ser positivo
        if (!valorAlvo.ehPositivo()) {
            throw new IllegalArgumentException("Valor alvo da meta deve ser positivo");
        }
        
        // Valor atual não pode ser negativo
        if (valorAtual.ehNegativo()) {
            throw new IllegalArgumentException("Valor atual da meta não pode ser negativo");
        }
        
        // Prazo não pode ser no passado (exceto para metas já criadas)
        if (criadoEm == null && prazo.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Prazo da meta não pode ser no passado");
        }
        
        // Valor atual não pode ser maior que valor alvo (exceto se meta já foi concluída)
        if (valorAtual.ehMaiorQue(valorAlvo) && !status.foiConcluida()) {
            throw new IllegalArgumentException("Valor atual não pode ser maior que valor alvo");
        }
    }
    
    /**
     * Adiciona progresso à meta financeira.
     */
    public void adicionarProgresso(Valor valorContribuicao) {
        Objects.requireNonNull(valorContribuicao, "Valor da contribuição não pode ser nulo");
        
        if (!valorContribuicao.ehPositivo()) {
            throw new IllegalArgumentException("Valor da contribuição deve ser positivo");
        }
        
        if (!status.podeReceberContribuicoes()) {
            throw new IllegalStateException("Meta não pode receber contribuições no status atual: " + status);
        }
        
        this.valorAtual = this.valorAtual.somar(valorContribuicao);
        this.atualizadoEm = LocalDateTime.now();
        
        // Verifica se a meta foi concluída
        if (valorAtual.ehMaiorOuIgualA(valorAlvo)) {
            this.status = StatusMeta.CONCLUIDA;
        }
    }
    
    /**
     * Remove progresso da meta financeira (quando transação é excluída/modificada).
     */
    public void removerProgresso(Valor valorRemocao) {
        Objects.requireNonNull(valorRemocao, "Valor da remoção não pode ser nulo");
        
        if (!valorRemocao.ehPositivo()) {
            throw new IllegalArgumentException("Valor da remoção deve ser positivo");
        }
        
        if (status.foiCancelada()) {
            throw new IllegalStateException("Não é possível remover progresso de meta cancelada");
        }
        
        Valor novoValor = this.valorAtual.subtrair(valorRemocao);
        
        // Não permite valor negativo
        if (novoValor.ehNegativo()) {
            this.valorAtual = Valor.zero(valorAtual.moeda());
        } else {
            this.valorAtual = novoValor;
        }
        
        this.atualizadoEm = LocalDateTime.now();
        
        // Se estava concluída e agora não está mais, volta para ativa
        if (status.foiConcluida() && valorAtual.ehMenorQue(valorAlvo)) {
            this.status = StatusMeta.ATIVA;
        }
    }
    
    /**
     * Atualiza o nome da meta.
     */
    public void atualizarNome(Nome novoNome) {
        Objects.requireNonNull(novoNome, "Novo nome não pode ser nulo");
        
        if (status.foiCancelada()) {
            throw new IllegalStateException("Não é possível atualizar meta cancelada");
        }
        
        this.nome = novoNome;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Atualiza o valor alvo da meta.
     */
    public void atualizarValorAlvo(Valor novoValorAlvo) {
        Objects.requireNonNull(novoValorAlvo, "Novo valor alvo não pode ser nulo");
        
        if (!novoValorAlvo.ehPositivo()) {
            throw new IllegalArgumentException("Valor alvo da meta deve ser positivo");
        }
        
        if (status.foiCancelada()) {
            throw new IllegalStateException("Não é possível atualizar meta cancelada");
        }
        
        this.valorAlvo = novoValorAlvo;
        this.atualizadoEm = LocalDateTime.now();
        
        // Recalcula status baseado no novo valor alvo
        if (valorAtual.ehMaiorOuIgualA(valorAlvo) && status.ehAtiva()) {
            this.status = StatusMeta.CONCLUIDA;
        } else if (status.foiConcluida() && valorAtual.ehMenorQue(valorAlvo)) {
            this.status = StatusMeta.ATIVA;
        }
    }
    
    /**
     * Atualiza o prazo da meta.
     */
    public void atualizarPrazo(LocalDate novoPrazo) {
        Objects.requireNonNull(novoPrazo, "Novo prazo não pode ser nulo");
        
        if (status.foiCancelada()) {
            throw new IllegalStateException("Não é possível atualizar meta cancelada");
        }
        
        this.prazo = novoPrazo;
        this.atualizadoEm = LocalDateTime.now();
        
        // Verifica se a meta venceu com o novo prazo
        verificarVencimento();
    }
    
    /**
     * Pausa a meta financeira.
     */
    public void pausar() {
        if (!status.ehAtiva()) {
            throw new IllegalStateException("Apenas metas ativas podem ser pausadas");
        }
        
        this.status = StatusMeta.PAUSADA;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Retoma a meta financeira pausada.
     */
    public void retomar() {
        if (!status.estaPausada()) {
            throw new IllegalStateException("Apenas metas pausadas podem ser retomadas");
        }
        
        this.status = StatusMeta.ATIVA;
        this.atualizadoEm = LocalDateTime.now();
        
        // Verifica se venceu enquanto estava pausada
        verificarVencimento();
    }
    
    /**
     * Cancela a meta financeira.
     */
    public void cancelar() {
        if (status.foiCancelada()) {
            throw new IllegalStateException("Meta já está cancelada");
        }
        
        if (status.foiConcluida()) {
            throw new IllegalStateException("Não é possível cancelar meta concluída");
        }
        
        this.status = StatusMeta.CANCELADA;
        this.atualizadoEm = LocalDateTime.now();
    }
    
    /**
     * Verifica se a meta venceu e atualiza o status se necessário.
     */
    public void verificarVencimento() {
        if (status.ehAtiva() && prazo.isBefore(LocalDate.now())) {
            this.status = StatusMeta.VENCIDA;
            this.atualizadoEm = LocalDateTime.now();
        }
    }
    
    /**
     * Calcula o percentual de conclusão da meta.
     */
    public BigDecimal calcularPercentualConclusao() {
        if (valorAlvo.ehZero()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal percentual = valorAtual.quantia()
            .divide(valorAlvo.quantia(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        
        // Limita a 100% para exibição
        return percentual.min(new BigDecimal("100"));
    }
    
    /**
     * Calcula o valor restante para atingir a meta.
     */
    public Valor calcularValorRestante() {
        if (valorAtual.ehMaiorOuIgualA(valorAlvo)) {
            return Valor.zero(valorAlvo.moeda());
        }
        
        return valorAlvo.subtrair(valorAtual);
    }
    
    /**
     * Estima a data de conclusão baseada no progresso atual.
     */
    public LocalDate estimarDataConclusao() {
        if (status.foiConcluida()) {
            return LocalDate.now();
        }
        
        if (valorAtual.ehZero() || !status.ehAtiva()) {
            return prazo; // Retorna o prazo original se não há progresso
        }
        
        // Calcula dias desde a criação
        long diasDecorridos = ChronoUnit.DAYS.between(criadoEm.toLocalDate(), LocalDate.now());
        
        if (diasDecorridos <= 0) {
            return prazo;
        }
        
        // Calcula progresso médio por dia
        BigDecimal progressoDiario = valorAtual.quantia()
            .divide(BigDecimal.valueOf(diasDecorridos), 4, RoundingMode.HALF_UP);
        
        if (progressoDiario.compareTo(BigDecimal.ZERO) <= 0) {
            return prazo;
        }
        
        // Calcula dias necessários para completar
        BigDecimal valorRestante = calcularValorRestante().quantia();
        long diasNecessarios = valorRestante
            .divide(progressoDiario, 0, RoundingMode.CEILING)
            .longValue();
        
        LocalDate dataEstimada = LocalDate.now().plusDays(diasNecessarios);
        
        // Não retorna data anterior a hoje
        return dataEstimada.isBefore(LocalDate.now()) ? LocalDate.now() : dataEstimada;
    }
    
    /**
     * Calcula quantos dias restam até o prazo.
     */
    public long calcularDiasRestantes() {
        return ChronoUnit.DAYS.between(LocalDate.now(), prazo);
    }
    
    /**
     * Verifica se a meta está atrasada (prazo vencido e não concluída).
     */
    public boolean estaAtrasada() {
        return prazo.isBefore(LocalDate.now()) && !status.foiConcluida();
    }
    
    /**
     * Verifica se a meta está próxima do prazo (últimos 30 dias).
     */
    public boolean estaProximaDoPrazo() {
        long diasRestantes = calcularDiasRestantes();
        return diasRestantes <= 30 && diasRestantes > 0 && status.ehAtiva();
    }
    
    /**
     * Verifica se a meta foi alcançada (valor atual >= valor alvo).
     */
    public boolean foiAlcancada() {
        return valorAtual.ehMaiorOuIgualA(valorAlvo);
    }
    
    // Getters
    public MetaId getId() {
        return id;
    }
    
    public UsuarioId getUsuarioId() {
        return usuarioId;
    }
    
    public Nome getNome() {
        return nome;
    }
    
    public Valor getValorAlvo() {
        return valorAlvo;
    }
    
    public Valor getValorAtual() {
        return valorAtual;
    }
    
    public LocalDate getPrazo() {
        return prazo;
    }
    
    public TipoMeta getTipo() {
        return tipo;
    }
    
    public StatusMeta getStatus() {
        return status;
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
        MetaFinanceira that = (MetaFinanceira) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("MetaFinanceira{id=%s, usuarioId=%s, nome=%s, valorAlvo=%s, valorAtual=%s, prazo=%s, tipo=%s, status=%s}", 
            id, usuarioId, nome, valorAlvo, valorAtual, prazo, tipo, status);
    }
}