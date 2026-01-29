package com.gestaofinanceira.application.ports.service;

import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Port para envio de notificações aos usuários.
 * 
 * Define as operações necessárias para notificar usuários sobre
 * eventos importantes relacionados às suas finanças.
 */
public interface NotificacaoPort {
    
    /**
     * Notifica usuário quando orçamento está próximo do limite (80%).
     * 
     * @param orcamento o orçamento próximo do limite
     */
    void notificarOrcamentoProximoLimite(Orcamento orcamento);
    
    /**
     * Notifica usuário quando orçamento excedeu o limite.
     * 
     * @param orcamento o orçamento que excedeu o limite
     */
    void notificarOrcamentoExcedido(Orcamento orcamento);
    
    /**
     * Notifica usuário quando meta financeira foi alcançada.
     * 
     * @param meta a meta que foi alcançada
     */
    void notificarMetaAlcancada(MetaFinanceira meta);
    
    /**
     * Notifica usuário quando prazo de meta está próximo sem conclusão.
     * 
     * @param meta a meta com prazo próximo
     * @param diasRestantes número de dias restantes
     */
    void notificarMetaPrazoProximo(MetaFinanceira meta, int diasRestantes);
    
    /**
     * Notifica usuário sobre conclusão da importação de planilha.
     * 
     * @param usuarioId ID do usuário
     * @param resultado resultado da importação
     */
    void notificarImportacaoConcluida(UsuarioId usuarioId, ResultadoImportacaoNotificacao resultado);
    
    /**
     * Notifica usuário sobre atividade suspeita detectada.
     * 
     * @param usuarioId ID do usuário
     * @param detalhesAtividade detalhes da atividade suspeita
     */
    void notificarAtividadeSuspeita(UsuarioId usuarioId, String detalhesAtividade);
    
    /**
     * Notifica usuário sobre insights de IA disponíveis.
     * 
     * @param usuarioId ID do usuário
     * @param insights lista de insights disponíveis
     */
    void notificarInsightsDisponiveis(UsuarioId usuarioId, List<InsightNotificacao> insights);
    
    /**
     * Envia notificação personalizada para o usuário.
     * 
     * @param notificacao dados da notificação personalizada
     */
    void enviarNotificacao(NotificacaoPersonalizada notificacao);
    
    /**
     * Representa resultado de importação para notificação.
     */
    record ResultadoImportacaoNotificacao(
        boolean sucesso,
        int transacoesCriadas,
        int duplicatasDetectadas,
        int errosEncontrados,
        String mensagemResumo
    ) {}
    
    /**
     * Representa insight para notificação.
     */
    record InsightNotificacao(
        String titulo,
        String resumo,
        TipoInsight tipo,
        NivelPrioridade prioridade
    ) {}
    
    /**
     * Representa notificação personalizada.
     */
    record NotificacaoPersonalizada(
        UsuarioId usuarioId,
        String titulo,
        String mensagem,
        TipoNotificacao tipo,
        NivelPrioridade prioridade,
        LocalDateTime agendadaPara,
        boolean requerAcao,
        String acaoUrl
    ) {}
    
    /**
     * Tipos de insight para notificação.
     */
    enum TipoInsight {
        ECONOMIA,
        ORCAMENTO,
        META,
        INVESTIMENTO,
        TENDENCIA
    }
    
    /**
     * Tipos de notificação.
     */
    enum TipoNotificacao {
        INFO,
        AVISO,
        ALERTA,
        SUCESSO,
        ERRO
    }
    
    /**
     * Níveis de prioridade para notificações.
     */
    enum NivelPrioridade {
        BAIXA,
        MEDIA,
        ALTA,
        URGENTE
    }
}