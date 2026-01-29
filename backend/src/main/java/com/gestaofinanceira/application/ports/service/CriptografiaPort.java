package com.gestaofinanceira.application.ports.service;

/**
 * Port para serviços de criptografia e segurança.
 * 
 * Define as operações necessárias para criptografia de dados sensíveis,
 * hashing de senhas e outras operações de segurança.
 */
public interface CriptografiaPort {
    
    /**
     * Gera hash seguro de uma senha.
     * 
     * @param senhaTextoPlano senha em texto plano
     * @return hash seguro da senha
     */
    String hashearSenha(String senhaTextoPlano);
    
    /**
     * Verifica se uma senha corresponde ao hash armazenado.
     * 
     * @param senhaTextoPlano senha em texto plano
     * @param hashArmazenado hash armazenado para comparação
     * @return true se a senha corresponde ao hash
     */
    boolean verificarSenha(String senhaTextoPlano, String hashArmazenado);
    
    /**
     * Criptografa dados sensíveis para armazenamento.
     * 
     * @param dadosTextoPlano dados em texto plano
     * @return dados criptografados
     */
    String criptografarDados(String dadosTextoPlano);
    
    /**
     * Descriptografa dados sensíveis armazenados.
     * 
     * @param dadosCriptografados dados criptografados
     * @return dados em texto plano
     */
    String descriptografarDados(String dadosCriptografados);
    
    /**
     * Gera token seguro para operações temporárias.
     * 
     * @param tamanho tamanho do token em bytes
     * @return token seguro gerado
     */
    String gerarTokenSeguro(int tamanho);
    
    /**
     * Gera salt para operações de hashing.
     * 
     * @return salt gerado
     */
    String gerarSalt();
    
    /**
     * Valida força de uma senha.
     * 
     * @param senha senha a ser validada
     * @return resultado da validação
     */
    ResultadoValidacaoSenha validarForcaSenha(String senha);
    
    /**
     * Representa resultado da validação de senha.
     */
    record ResultadoValidacaoSenha(
        boolean valida,
        int pontuacao,
        NivelForca nivel,
        java.util.List<String> sugestoesMelhoria
    ) {}
    
    /**
     * Níveis de força de senha.
     */
    enum NivelForca {
        MUITO_FRACA,
        FRACA,
        MEDIA,
        FORTE,
        MUITO_FORTE
    }
}