package com.gestaofinanceira.application.usecases.meta;

import com.gestaofinanceira.application.dto.command.ComandoCriarMetaFinanceira;
import com.gestaofinanceira.application.ports.repository.MetaFinanceiraRepository;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.domain.entities.MetaFinanceira;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Use Case para criação de metas financeiras.
 * 
 * Implementa o requirement 6.1: "WHEN a user creates a financial goal, 
 * THE Sistema SHALL validate goal parameters (name, target amount, deadline)"
 * 
 * Responsabilidades:
 * - Validar dados da meta
 * - Verificar se o prazo é futuro
 * - Criar entidade MetaFinanceira
 * - Salvar no repositório
 */
@Service
@Transactional
public class CriarMetaFinanceiraUseCase {
    
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    private final UsuarioRepository usuarioRepository;
    
    public CriarMetaFinanceiraUseCase(MetaFinanceiraRepository metaFinanceiraRepository,
                                      UsuarioRepository usuarioRepository) {
        this.metaFinanceiraRepository = Objects.requireNonNull(metaFinanceiraRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }
    
    /**
     * Executa a criação de uma nova meta financeira.
     * 
     * @param comando dados para criação da meta
     * @return a meta criada
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws IllegalStateException se o usuário não existir
     */
    public MetaFinanceira executar(ComandoCriarMetaFinanceira comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // 1. Validar usuário
        Usuario usuario = validarUsuario(comando.usuarioId());
        
        // 2. Criar value objects
        Nome nome = new Nome(comando.nome());
        Valor valorAlvo = Valor.reais(comando.valorAlvo());
        
        // 3. Validar prazo
        validarPrazo(comando.prazo());
        
        // 4. Criar meta financeira
        MetaFinanceira meta = MetaFinanceira.criar(
            comando.usuarioId(),
            nome,
            valorAlvo,
            comando.prazo(),
            comando.tipo()
        );
        
        // 5. Salvar meta
        return metaFinanceiraRepository.salvar(meta);
    }
    
    /**
     * Valida se o usuário existe e está ativo.
     */
    private Usuario validarUsuario(UsuarioId usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
            .filter(Usuario::isAtivo)
            .orElseThrow(() -> new IllegalStateException(
                "Usuário não encontrado ou inativo: " + usuarioId));
    }
    
    /**
     * Valida se o prazo é futuro.
     */
    private void validarPrazo(java.time.LocalDate prazo) {
        if (prazo.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Prazo deve ser uma data futura");
        }
        
        // Validar se o prazo não é muito distante (máximo 10 anos)
        if (prazo.isAfter(java.time.LocalDate.now().plusYears(10))) {
            throw new IllegalArgumentException("Prazo não pode ser superior a 10 anos");
        }
    }
}