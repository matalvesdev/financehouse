package com.gestaofinanceira.application.usecases.autenticacao;

import com.gestaofinanceira.application.dto.command.ComandoCriarUsuario;
import com.gestaofinanceira.application.dto.response.UsuarioResponse;
import com.gestaofinanceira.application.ports.repository.UsuarioRepository;
import com.gestaofinanceira.application.ports.service.CriptografiaPort;
import com.gestaofinanceira.domain.entities.Usuario;
import com.gestaofinanceira.domain.valueobjects.Email;
import com.gestaofinanceira.domain.valueobjects.Nome;
import com.gestaofinanceira.domain.valueobjects.SenhaHash;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Caso de uso para registro de novos usuários no sistema.
 * 
 * Implementa o processo completo de criação de conta:
 * - Validação de dados de entrada
 * - Verificação de unicidade do email
 * - Validação de força da senha
 * - Criação segura do usuário
 * - Persistência no repositório
 * 
 * Requirements: 1.5, 1.6, 10.1
 */
@Service
@Transactional
public class RegistrarUsuarioUseCase {
    
    private final UsuarioRepository usuarioRepository;
    private final CriptografiaPort criptografiaPort;
    
    public RegistrarUsuarioUseCase(UsuarioRepository usuarioRepository, 
                                   CriptografiaPort criptografiaPort) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, 
            "UsuarioRepository não pode ser nulo");
        this.criptografiaPort = Objects.requireNonNull(criptografiaPort, 
            "CriptografiaPort não pode ser nulo");
    }
    
    /**
     * Executa o registro de um novo usuário.
     * 
     * @param comando dados para criação do usuário
     * @return dados do usuário criado
     * @throws IllegalArgumentException se os dados são inválidos
     * @throws IllegalStateException se o email já está em uso
     */
    public UsuarioResponse executar(ComandoCriarUsuario comando) {
        Objects.requireNonNull(comando, "Comando não pode ser nulo");
        
        // 1. Validar dados de entrada
        validarDadosEntrada(comando);
        
        // 2. Criar value objects
        Email email = new Email(comando.email());
        Nome nome = new Nome(comando.nome());
        
        // 3. Verificar unicidade do email
        if (usuarioRepository.existePorEmail(email)) {
            throw new IllegalStateException("Email já está em uso: " + email.valor());
        }
        
        // 4. Validar força da senha
        var resultadoValidacao = criptografiaPort.validarForcaSenha(comando.senha());
        if (!resultadoValidacao.valida()) {
            throw new IllegalArgumentException(
                "Senha não atende aos critérios de segurança: " + 
                String.join(", ", resultadoValidacao.sugestoesMelhoria())
            );
        }
        
        // 5. Criar hash seguro da senha
        SenhaHash senhaHash = SenhaHash.criarDeSenhaTexto(comando.senha());
        
        // 6. Criar entidade Usuario
        Usuario usuario = Usuario.criar(email, senhaHash, nome);
        
        // 7. Persistir no repositório
        Usuario usuarioSalvo = usuarioRepository.salvar(usuario);
        
        // 8. Retornar resposta
        return new UsuarioResponse(
            usuarioSalvo.getId().valor().toString(),
            usuarioSalvo.getNome().valor(),
            usuarioSalvo.getEmail().valor(),
            usuarioSalvo.getCriadoEm(),
            usuarioSalvo.isAtivo(),
            usuarioSalvo.isDadosIniciaisCarregados()
        );
    }
    
    /**
     * Valida os dados de entrada do comando.
     */
    private void validarDadosEntrada(ComandoCriarUsuario comando) {
        if (comando.nome() == null || comando.nome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (comando.email() == null || comando.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        
        if (comando.senha() == null || comando.senha().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        
        // Validação básica de formato de email será feita pelo value object Email
        // Validação de força da senha será feita pelo CriptografiaPort
    }
}