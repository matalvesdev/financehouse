# Checklist de Deploy - Oracle Cloud Infrastructure

Use este checklist para garantir um deploy bem-sucedido na OCI.

## ☑️ Pré-Deploy

### Conta OCI
- [ ] Conta OCI criada (https://www.oracle.com/cloud/free/)
- [ ] Email verificado
- [ ] MFA habilitado (recomendado)
- [ ] Compartment criado (ou usando root)

### Ferramentas Locais
- [ ] Terraform >= 1.0 instalado
- [ ] Git instalado
- [ ] SSH client instalado
- [ ] Make instalado (opcional, mas recomendado)

### Credenciais OCI
- [ ] Tenancy OCID copiado
- [ ] User OCID copiado
- [ ] Compartment OCID copiado
- [ ] Região selecionada (ex: sa-saopaulo-1)

### Chaves
- [ ] Chave API OCI gerada
- [ ] Chave pública adicionada no console OCI
- [ ] Fingerprint da chave anotado
- [ ] Chave SSH gerada para acesso à instância

### Senhas e Secrets
- [ ] Senha do admin do banco definida (mín. 12 caracteres)
- [ ] Senha do usuário do banco definida
- [ ] JWT secret gerado (32 caracteres)
- [ ] Encryption key gerado (32 caracteres)

## ☑️ Configuração Terraform

### Arquivos
- [ ] Repositório clonado
- [ ] Navegado para pasta `terraform/`
- [ ] Arquivo `terraform.tfvars` criado (copiar de `.example`)
- [ ] Todas as variáveis preenchidas em `terraform.tfvars`
- [ ] Arquivo `terraform.tfvars` NÃO commitado no Git

### Validação
- [ ] `terraform init` executado com sucesso
- [ ] `terraform validate` passou
- [ ] `terraform fmt` executado
- [ ] `terraform plan` revisado

## ☑️ Deploy

### Execução
- [ ] `terraform apply` executado
- [ ] Plano revisado antes de confirmar
- [ ] Confirmado com `yes`
- [ ] Deploy concluído sem erros (~10-15 min)

### Outputs
- [ ] IP público da instância anotado
- [ ] Connection string do banco anotada
- [ ] Wallet do banco baixado (`wallet.zip`)
- [ ] Comando SSH testado

## ☑️ Pós-Deploy

### Acesso à Instância
- [ ] Conexão SSH funcionando
- [ ] Docker instalado e rodando
- [ ] Docker Compose instalado
- [ ] Diretório `/opt/gestao-financeira/` existe

### Configuração do Banco
- [ ] Wallet copiado para instância
- [ ] Wallet extraído em `/opt/gestao-financeira/wallet/`
- [ ] Permissões do wallet configuradas (600)
- [ ] Conexão com banco testada

### Deploy da Aplicação
- [ ] Script de deploy editado com URL do repositório
- [ ] Repositório clonado na instância
- [ ] Variáveis de ambiente configuradas
- [ ] Docker Compose executado
- [ ] Containers rodando

### Verificação
- [ ] Health check respondendo (`:8080/actuator/health`)
- [ ] Frontend acessível (porta 80)
- [ ] Backend acessível (porta 8080)
- [ ] Logs sem erros críticos

## ☑️ Segurança

### Network
- [ ] Security lists configuradas corretamente
- [ ] Apenas portas necessárias abertas (22, 80, 443, 8080)
- [ ] Banco de dados na subnet privada
- [ ] NAT Gateway funcionando

### SSH
- [ ] Login via senha desabilitado
- [ ] Apenas chave SSH permitida
- [ ] Fail2ban instalado (opcional)
- [ ] Firewall configurado

### Aplicação
- [ ] Secrets em variáveis de ambiente (não hardcoded)
- [ ] Arquivo `.env` com permissões 600
- [ ] HTTPS configurado (recomendado)
- [ ] JWT secret forte e único

### Banco de Dados
- [ ] Senha forte configurada
- [ ] Conexão via wallet (mTLS)
- [ ] Backups automáticos habilitados
- [ ] Acesso apenas da subnet pública

## ☑️ Monitoramento

### Métricas
- [ ] Métricas da instância visíveis no console OCI
- [ ] Métricas do banco visíveis no console OCI
- [ ] Spring Boot Actuator configurado
- [ ] Logs sendo gerados

### Alertas
- [ ] Alerta de CPU > 80% configurado
- [ ] Alerta de memória > 90% configurado
- [ ] Alerta de storage do banco > 80% configurado
- [ ] Notificações configuradas (email/SMS)

### Logs
- [ ] Logs da aplicação acessíveis
- [ ] Logs do sistema acessíveis
- [ ] Rotação de logs configurada
- [ ] Logs de auditoria habilitados

## ☑️ Backup

### Banco de Dados
- [ ] Backups automáticos habilitados
- [ ] Retenção de 60 dias configurada
- [ ] Backup manual testado
- [ ] Restauração testada (ambiente de teste)

### Instância
- [ ] Boot volume backup policy aplicada
- [ ] Backup semanal configurado
- [ ] Retenção de 4 semanas
- [ ] Snapshot manual criado

### Aplicação
- [ ] Código no Git
- [ ] Variáveis de ambiente documentadas
- [ ] Configurações documentadas
- [ ] Procedimento de restauração documentado

## ☑️ Documentação

### Interna
- [ ] IPs e credenciais documentados (local seguro)
- [ ] Procedimentos de deploy documentados
- [ ] Procedimentos de rollback documentados
- [ ] Contatos de emergência documentados

### Equipe
- [ ] Equipe treinada no acesso SSH
- [ ] Equipe sabe como ver logs
- [ ] Equipe sabe como reiniciar aplicação
- [ ] Equipe sabe como fazer rollback

## ☑️ Testes

### Funcionalidade
- [ ] Login funcionando
- [ ] Registro de usuário funcionando
- [ ] CRUD de transações funcionando
- [ ] CRUD de orçamentos funcionando
- [ ] CRUD de metas funcionando
- [ ] Importação de planilhas funcionando

### Performance
- [ ] Tempo de resposta < 2s
- [ ] Aplicação suporta 10 usuários simultâneos
- [ ] Banco de dados respondendo rápido
- [ ] Sem memory leaks

### Segurança
- [ ] Autenticação obrigatória
- [ ] Tokens JWT funcionando
- [ ] Refresh tokens funcionando
- [ ] Logout invalidando tokens
- [ ] Dados sensíveis criptografados

## ☑️ CI/CD (Opcional)

### GitHub Actions
- [ ] Secrets configurados no GitHub
- [ ] Workflow de deploy criado
- [ ] Workflow de rollback criado
- [ ] Deploy automático testado
- [ ] Rollback testado

### Monitoramento
- [ ] Notificações de deploy configuradas
- [ ] Métricas de deploy coletadas
- [ ] Logs de deploy acessíveis

## ☑️ Produção

### DNS (se aplicável)
- [ ] Domínio apontado para IP público
- [ ] Registro A configurado
- [ ] Registro CNAME configurado (www)
- [ ] TTL configurado adequadamente
- [ ] Propagação DNS verificada

### HTTPS (recomendado)
- [ ] Certbot instalado
- [ ] Certificado Let's Encrypt obtido
- [ ] Renovação automática configurada
- [ ] Redirect HTTP → HTTPS configurado
- [ ] HSTS configurado

### Performance
- [ ] CDN configurado (opcional)
- [ ] Cache configurado
- [ ] Compressão gzip habilitada
- [ ] Assets otimizados

## ☑️ Manutenção

### Rotina Diária
- [ ] Verificar logs de erro
- [ ] Verificar métricas de CPU/RAM
- [ ] Verificar health checks
- [ ] Verificar alertas

### Rotina Semanal
- [ ] Revisar logs de acesso
- [ ] Verificar backups
- [ ] Atualizar dependências (se necessário)
- [ ] Revisar security lists

### Rotina Mensal
- [ ] Atualizar sistema operacional
- [ ] Rotacionar secrets
- [ ] Revisar custos (deve ser R$ 0)
- [ ] Testar procedimento de recuperação
- [ ] Revisar documentação

## ☑️ Troubleshooting

### Problemas Comuns
- [ ] Documentado: Instância não responde
- [ ] Documentado: Aplicação não inicia
- [ ] Documentado: Banco de dados inacessível
- [ ] Documentado: Erro de autenticação
- [ ] Documentado: Erro de memória

### Contatos
- [ ] Suporte OCI: https://support.oracle.com
- [ ] Documentação OCI: https://docs.oracle.com/en-us/iaas/
- [ ] Comunidade: https://community.oracle.com

## 📝 Notas

### Informações Importantes

**IP Público**: ___________________________

**Database Connection**: ___________________________

**Wallet Location**: ___________________________

**Deploy Date**: ___________________________

**Deployed By**: ___________________________

### Próximos Passos

1. ___________________________
2. ___________________________
3. ___________________________

### Observações

___________________________
___________________________
___________________________

---

## ✅ Deploy Completo

- [ ] **Todos os itens acima verificados**
- [ ] **Aplicação em produção**
- [ ] **Equipe notificada**
- [ ] **Documentação atualizada**

**Assinatura**: ___________________________

**Data**: ___________________________

---

**Dica**: Imprima este checklist ou mantenha uma cópia digital para cada deploy.
