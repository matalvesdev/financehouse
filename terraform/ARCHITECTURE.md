# Arquitetura da Infraestrutura OCI

Este documento descreve a arquitetura da infraestrutura na Oracle Cloud Infrastructure (OCI) para a aplicação de Gestão Financeira Doméstica.

## Visão Geral

A infraestrutura utiliza recursos **Always Free** da OCI, garantindo custo zero e alta disponibilidade para aplicações de pequeno a médio porte.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Oracle Cloud (OCI)                        │
│                     Region: sa-saopaulo-1                        │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 │
┌─────────────────────────────────────────────────────────────────┐
│                    Virtual Cloud Network (VCN)                   │
│                        CIDR: 10.0.0.0/16                         │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Internet Gateway                         │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                 │                                 │
│  ┌──────────────────────────────┴──────────────────────────────┐│
│  │              Public Subnet (10.0.1.0/24)                    ││
│  │                                                              ││
│  │  ┌────────────────────────────────────────────────────┐    ││
│  │  │         Compute Instance (Always Free)             │    ││
│  │  │    VM.Standard.E2.1.Micro (1 OCPU, 1GB RAM)       │    ││
│  │  │                                                     │    ││
│  │  │  ┌──────────────────────────────────────────┐     │    ││
│  │  │  │         Docker Containers                 │     │    ││
│  │  │  │                                            │     │    ││
│  │  │  │  ┌────────────┐    ┌──────────────┐     │     │    ││
│  │  │  │  │  Frontend  │    │   Backend    │     │     │    ││
│  │  │  │  │   (Nginx)  │◄───┤  (Spring)    │     │     │    ││
│  │  │  │  │   Port 80  │    │  Port 8080   │     │     │    ││
│  │  │  │  └────────────┘    └──────┬───────┘     │     │    ││
│  │  │  │                            │              │     │    ││
│  │  │  └────────────────────────────┼──────────────┘     │    ││
│  │  │                                │                     │    ││
│  │  └────────────────────────────────┼─────────────────────┘    ││
│  │                                    │                          ││
│  └────────────────────────────────────┼──────────────────────────┘│
│                                       │                            │
│  ┌────────────────────────────────────┼──────────────────────────┐│
│  │              NAT Gateway           │                          ││
│  └────────────────────────────────────┼──────────────────────────┘│
│                                       │                            │
│  ┌────────────────────────────────────┼──────────────────────────┐│
│  │           Service Gateway          │                          ││
│  └────────────────────────────────────┼──────────────────────────┘│
│                                       │                            │
│  ┌────────────────────────────────────┴──────────────────────────┐│
│  │             Private Subnet (10.0.2.0/24)                      ││
│  │                                                                ││
│  │  ┌──────────────────────────────────────────────────────┐    ││
│  │  │      Autonomous Database (Always Free)               │    ││
│  │  │         Oracle Database 19c                          │    ││
│  │  │         1 OCPU, 20GB Storage                         │    ││
│  │  │         Backups Automáticos                          │    ││
│  │  └──────────────────────────────────────────────────────┘    ││
│  │                                                                ││
│  └────────────────────────────────────────────────────────────────┘│
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Componentes

### 1. Network Layer

#### Virtual Cloud Network (VCN)
- **CIDR Block**: 10.0.0.0/16
- **DNS Label**: gestaofinanceira
- **Função**: Rede virtual isolada para todos os recursos

#### Subnets

##### Public Subnet (10.0.1.0/24)
- **Tipo**: Pública (com IP público)
- **Recursos**: Instâncias de aplicação
- **Acesso**: Internet via Internet Gateway
- **Security List**: Permite HTTP (80), HTTPS (443), SSH (22)

##### Private Subnet (10.0.2.0/24)
- **Tipo**: Privada (sem IP público)
- **Recursos**: Banco de dados
- **Acesso**: Internet via NAT Gateway
- **Security List**: Permite apenas tráfego da subnet pública

#### Gateways

##### Internet Gateway
- **Função**: Permite acesso à internet para recursos na subnet pública
- **Tráfego**: Bidirecional (entrada e saída)

##### NAT Gateway
- **Função**: Permite acesso à internet para recursos na subnet privada
- **Tráfego**: Apenas saída (segurança)

##### Service Gateway
- **Função**: Acesso a serviços OCI sem passar pela internet pública
- **Serviços**: Object Storage, Autonomous Database, etc.

### 2. Compute Layer

#### Instância de Aplicação

**Especificações (Always Free)**:
- **Shape**: VM.Standard.E2.1.Micro
  - 1 OCPU (AMD)
  - 1 GB RAM
  - 0.48 Gbps network bandwidth
- **Alternativa**: VM.Standard.A1.Flex
  - Até 4 OCPUs (ARM Ampere A1)
  - Até 24 GB RAM
  - Melhor performance, mas ARM

**Sistema Operacional**:
- Oracle Linux 8
- Atualizações automáticas de segurança
- Firewall configurado

**Software Instalado**:
- Docker & Docker Compose
- Oracle Instant Client
- Git, curl, wget, unzip

**Containers Docker**:
1. **Frontend** (Nginx)
   - Serve aplicação React
   - Proxy reverso para backend
   - Porta 80 (HTTP)

2. **Backend** (Spring Boot)
   - API REST
   - Conexão com banco de dados
   - Porta 8080

### 3. Database Layer

#### Autonomous Database (Always Free)

**Especificações**:
- **Tipo**: OLTP (Online Transaction Processing)
- **Versão**: Oracle Database 19c
- **CPU**: 1 OCPU
- **Storage**: 20 GB
- **Licença**: Incluída (LICENSE_INCLUDED)

**Características**:
- ✅ Backups automáticos diários
- ✅ Patches automáticos
- ✅ Auto-scaling desabilitado (Always Free)
- ✅ Criptografia em repouso
- ✅ Conexão via wallet (mTLS)

**Network**:
- Localizado na subnet privada
- Acesso apenas da subnet pública
- Network Security Group dedicado

### 4. Security Layer

#### Network Security

**Security Lists**:
- Public Subnet:
  - Ingress: HTTP (80), HTTPS (443), SSH (22)
  - Egress: All traffic
- Private Subnet:
  - Ingress: Oracle DB (1521-1522) apenas da subnet pública
  - Egress: All traffic

**Network Security Groups**:
- Database NSG:
  - Regras granulares para acesso ao banco
  - Isolamento adicional

#### Application Security

**Autenticação**:
- JWT tokens com secret rotacionável
- Refresh tokens para sessões longas
- Logout com invalidação de tokens

**Criptografia**:
- Dados sensíveis criptografados em repouso
- Conexão com banco via mTLS (wallet)
- HTTPS recomendado (Let's Encrypt)

**Secrets Management**:
- Variáveis de ambiente para secrets
- Arquivo .env com permissões restritas (600)
- Nunca commitar secrets no Git

### 5. Deployment Layer

#### Cloud-Init
- Configuração automática da instância
- Instalação de dependências
- Setup de Docker e aplicação
- Configuração de firewall

#### Systemd Service
- Auto-start da aplicação no boot
- Gerenciamento via systemctl
- Logs centralizados

#### Deploy Script
- Script automatizado de deploy
- Pull do repositório Git
- Build e restart dos containers
- Rollback em caso de falha

## Fluxo de Dados

### Request Flow (Usuário → Aplicação)

```
1. Usuário acessa http://<IP_PUBLICO>
   ↓
2. Internet Gateway encaminha para instância na subnet pública
   ↓
3. Nginx (container frontend) recebe request na porta 80
   ↓
4. Se for API call, Nginx faz proxy para backend:8080
   ↓
5. Spring Boot (container backend) processa request
   ↓
6. Backend conecta ao Autonomous Database via wallet
   ↓
7. Database processa query e retorna dados
   ↓
8. Backend retorna response para frontend
   ↓
9. Frontend renderiza e retorna para usuário
```

### Database Connection Flow

```
1. Backend inicia conexão com banco
   ↓
2. Usa wallet (mTLS) para autenticação
   ↓
3. Tráfego vai pela subnet privada
   ↓
4. Network Security Group valida origem
   ↓
5. Autonomous Database aceita conexão
   ↓
6. Conexão criptografada estabelecida
```

## Recursos Always Free

### Limites e Quotas

**Compute**:
- 2x VM.Standard.E2.1.Micro (AMD) OU
- 4 OCPUs ARM (VM.Standard.A1.Flex)
- 24 GB RAM total (ARM)

**Database**:
- 2x Autonomous Databases
- 1 OCPU cada
- 20 GB storage cada

**Storage**:
- 200 GB Block Volume
- 10 GB Object Storage
- 10 GB Archive Storage

**Network**:
- 10 TB outbound data transfer/mês
- VCN, subnets, gateways ilimitados

**Outros**:
- Load Balancer: 1x (10 Mbps)
- Monitoring: Métricas e alarmes
- Notifications: Email e SMS

### Custos

**Total: R$ 0,00/mês** 🎉

Todos os recursos utilizados estão dentro do tier Always Free da OCI, que:
- ✅ Não expira
- ✅ Não requer cartão de crédito após trial
- ✅ Disponível em todas as regiões

## Alta Disponibilidade

### Estratégias Implementadas

1. **Autonomous Database**:
   - Backups automáticos diários
   - Recuperação point-in-time
   - Patches automáticos sem downtime

2. **Compute Instance**:
   - Boot volume backup policy (bronze)
   - Cloud-init para reconfiguração rápida
   - Systemd para auto-restart

3. **Application**:
   - Docker para isolamento
   - Health checks configurados
   - Restart automático em caso de falha

### Limitações (Always Free)

- ❌ Sem load balancer redundante
- ❌ Sem múltiplas availability domains
- ❌ Sem auto-scaling
- ❌ Sem failover automático

**Recomendação**: Para produção crítica, considere upgrade para recursos pagos.

## Monitoramento

### Métricas Disponíveis

**Compute**:
- CPU utilization
- Memory utilization
- Network I/O
- Disk I/O

**Database**:
- CPU utilization
- Storage utilization
- Connection count
- Query performance

**Application**:
- Spring Boot Actuator metrics
- Docker container stats
- Application logs

### Alertas

Configure alertas no console OCI:
- CPU > 80% por 5 minutos
- Memory > 90% por 5 minutos
- Database storage > 80%
- Application health check failures

## Backup e Recuperação

### Autonomous Database

**Backups Automáticos**:
- Diários, retidos por 60 dias
- Recuperação point-in-time
- Sem custo adicional

**Backup Manual**:
```bash
# Via console OCI
Database → Autonomous Database → More Actions → Create Manual Backup
```

### Compute Instance

**Boot Volume Backup**:
- Policy: Bronze (backup semanal)
- Retenção: 4 semanas
- Restauração via console

**Application Data**:
```bash
# Backup de volumes Docker
docker run --rm -v gestao-financeira_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/data-backup-$(date +%Y%m%d).tar.gz /data
```

## Escalabilidade

### Vertical Scaling

**Compute**:
- Upgrade para shapes maiores (pago)
- Adicionar OCPUs/RAM (Flex shapes)

**Database**:
- Upgrade para mais OCPUs (pago)
- Aumentar storage (pago)

### Horizontal Scaling

**Compute**:
- Adicionar mais instâncias
- Configurar load balancer
- Implementar session sharing

**Database**:
- Read replicas (pago)
- Sharding (complexo)

## Segurança Best Practices

### Network

- ✅ Subnets separadas (pública/privada)
- ✅ Security lists restritivas
- ✅ NSGs para controle granular
- ✅ NAT Gateway para subnet privada

### Compute

- ✅ SSH apenas com chave (sem senha)
- ✅ Firewall configurado
- ✅ Atualizações automáticas
- ✅ Fail2ban para proteção SSH

### Database

- ✅ Subnet privada (sem acesso direto)
- ✅ Conexão via wallet (mTLS)
- ✅ Senhas fortes
- ✅ Criptografia em repouso

### Application

- ✅ JWT para autenticação
- ✅ Secrets em variáveis de ambiente
- ✅ Criptografia de dados sensíveis
- ✅ HTTPS recomendado

## Manutenção

### Rotina Diária
- Verificar logs de aplicação
- Monitorar métricas de CPU/RAM
- Verificar health checks

### Rotina Semanal
- Revisar alertas
- Verificar backups
- Atualizar dependências

### Rotina Mensal
- Atualizar sistema operacional
- Revisar security lists
- Rotacionar secrets
- Testar recuperação de backup

## Custos de Upgrade

Se precisar escalar além do Always Free:

**Compute**:
- VM.Standard.E2.1: ~$0.05/hora (~$36/mês)
- VM.Standard.E4.Flex (1 OCPU): ~$0.03/hora (~$22/mês)

**Database**:
- Autonomous Database (1 OCPU): ~$0.30/hora (~$216/mês)
- Storage adicional: ~$0.025/GB/mês

**Load Balancer**:
- 10 Mbps: Grátis
- 100 Mbps: ~$10/mês
- 400 Mbps: ~$30/mês

## Referências

- [OCI Always Free](https://www.oracle.com/cloud/free/)
- [OCI Documentation](https://docs.oracle.com/en-us/iaas/)
- [Terraform OCI Provider](https://registry.terraform.io/providers/oracle/oci/)
- [Autonomous Database](https://docs.oracle.com/en/cloud/paas/autonomous-database/)
- [OCI Networking](https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/overview.htm)

---

Última atualização: Janeiro 2026
