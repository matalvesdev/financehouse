package com.gestaofinanceira.application.ports.repository;

import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.valueobjects.MetaId;
import com.gestaofinanceira.domain.valueobjects.StatusMeta;
import com.gestaofinanceira.domain.valueobjects.TipoMeta;
import com.gestaofinanceira.domain.valueobjects.UsuarioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port para persistência de metas financeiras.
 * 
 * Define as operações de persistência necessárias para a entidade MetaFinanceira,
 * incluindo consultas para acompanhamento de progresso e notificações.
 */
public interface MetaFinanceiraRepository {
    
    /**
     * Salva uma meta financeira no repositório.
     * 
     * @param meta a meta a ser salva
     * @return a meta salva com ID gerado
     */
    MetaFinanceira salvar(MetaFinanceira meta);
    
    /**
     * Busca uma meta por ID.
     * 
     * @param id o ID da meta
     * @return Optional contendo a meta se encontrada
     */
    Optional<MetaFinanceira> buscarPorId(MetaId id);
    
    /**
     * Busca metas ativas de um usuário.
     * 
     * @param usuarioId o ID do usuário
     * @return lista de metas ativas
     */
    List<MetaFinanceira> buscarAtivasPorUsuario(UsuarioId usuarioId);
    
    /**
     * Busca todas as metas de um usuário (ativas e inativas).
     * 
     * @param usuarioId o ID do usuário
     * @return lista de todas as metas do usuário
     */
    List<MetaFinanceira> buscarPorUsuario(UsuarioId usuarioId);
    
    /**
     * Busca metas por status.
     * 
     * @param usuarioId o ID do usuário
     * @param status o status das metas
     * @return lista de metas com o status especificado
     */
    List<MetaFinanceira> buscarPorUsuarioEStatus(UsuarioId usuarioId, StatusMeta status);
    
    /**
     * Busca metas por tipo.
     * 
     * @param usuarioId o ID do usuário
     * @param tipo o tipo das metas
     * @return lista de metas do tipo especificado
     */
    List<MetaFinanceira> buscarPorUsuarioETipo(UsuarioId usuarioId, TipoMeta tipo);
    
    /**
     * Busca metas que vencem em breve (próximos 30 dias).
     * 
     * @param usuarioId o ID do usuário
     * @return lista de metas com prazo próximo
     */
    List<MetaFinanceira> buscarComPrazoProximo(UsuarioId usuarioId);
    
    /**
     * Busca metas que vencem em uma data específica.
     * 
     * @param data a data de vencimento
     * @return lista de metas que vencem na data
     */
    List<MetaFinanceira> buscarVencendoEm(LocalDate data);
    
    /**
     * Busca metas alcançadas recentemente.
     * 
     * @param usuarioId o ID do usuário
     * @param diasRecentes número de dias para considerar "recente"
     * @return lista de metas alcançadas recentemente
     */
    List<MetaFinanceira> buscarAlcancadasRecentemente(UsuarioId usuarioId, int diasRecentes);
    
    /**
     * Atualiza uma meta existente.
     * 
     * @param meta a meta com dados atualizados
     * @return a meta atualizada
     */
    MetaFinanceira atualizar(MetaFinanceira meta);
    
    /**
     * Remove uma meta do repositório.
     * 
     * @param id o ID da meta a ser removida
     */
    void remover(MetaId id);
    
    /**
     * Arquiva metas vencidas não alcançadas.
     * 
     * @param dataLimite data limite para arquivamento
     * @return número de metas arquivadas
     */
    int arquivarVencidas(LocalDate dataLimite);
}