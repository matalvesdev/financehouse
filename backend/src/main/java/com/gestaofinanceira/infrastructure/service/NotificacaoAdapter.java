package com.gestaofinanceira.infrastructure.service;

import com.gestaofinanceira.application.ports.service.NotificacaoPort;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Orcamento;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementação mock do serviço de notificações para desenvolvimento local.
 * Em produção, deve ser substituído por implementação real (email, SMS, push, etc.)
 */
@Service
public class NotificacaoAdapter implements NotificacaoPort {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoAdapter.class);
    
    @Override
    public void notificarOrcamentoProximoLimite(Orcamento orcamento) {
        logger.warn("⚠️ [MOCK] Orçamento próximo do limite: {} - {}%", 
                   orcamento.getCategoria(), 80);
    }
    
    @Override
    public void notificarOrcamentoExcedido(Orcamento orcamento) {
        logger.error("🚨 [MOCK] Orçamento excedido: {}", orcamento.getCategoria());
    }
    
    @Override
    public void notificarMetaAlcancada(MetaFinanceira meta) {
        logger.info("🎯 [MOCK] Meta alcançada: {}", meta.getNome());
    }
    
    @Override
    public void notificarMetaPrazoProximo(MetaFinanceira meta, int diasRestantes) {
        logger.warn("⏰ [MOCK] Meta com prazo próximo: {} - {} dias restantes", 
                   meta.getNome(), diasRestantes);
    }
    
    @Override
    public void notificarImportacaoConcluida(UsuarioId usuarioId, ResultadoImportacaoNotificacao resultado) {
        logger.info("📊 [MOCK] Importação concluída para usuário {}: {} transações criadas", 
                   usuarioId.valor(), resultado.transacoesCriadas());
    }
    
    @Override
    public void notificarAtividadeSuspeita(UsuarioId usuarioId, String detalhesAtividade) {
        logger.warn("🔒 [MOCK] Atividade suspeita detectada para usuário {}: {}", 
                   usuarioId.valor(), detalhesAtividade);
    }
    
    @Override
    public void notificarInsightsDisponiveis(UsuarioId usuarioId, List<InsightNotificacao> insights) {
        logger.info("💡 [MOCK] {} insights disponíveis para usuário {}", 
                   insights.size(), usuarioId.valor());
    }
    
    @Override
    public void enviarNotificacao(NotificacaoPersonalizada notificacao) {
        logger.info("📧 [MOCK] Notificação enviada para usuário {}: {} - {}", 
                   notificacao.usuarioId().valor(), 
                   notificacao.titulo(), 
                   notificacao.mensagem());
    }
}
