# Deploy na Oracle Cloud Infrastructure (OCI)

Este documento descreve como fazer deploy da aplicação de Gestão Financeira Doméstica na Oracle Cloud Infrastructure usando recursos **Always Free** (custo zero).

## 📋 Visão Geral

A infraestrutura Terraform criada provisiona:

- ✅ **Compute Instance** (VM.Standard.E2.1.Micro - Always Free)
  - 1 OCPU, 1GB RAM
  - Oracle Linux 8
  - Docker + Docker Compose pré-instalado

- ✅ **Autonomous Database** (Always Free)
  - Oracle Database 19c
  - 1 OCPU, 20GB storage
  - Backups automáticos

- ✅ **Network Infrastructure**
  - VCN com subnets públicas e privadas
  - Internet Gateway, NAT Gateway, Service Gateway
  - Security Lists e Network Security Groups

**Custo Total: R$ 0,00/mês** 🎉

## 🚀 Quick Start

### Opção 1: Setup Multi-Ambiente (Recomendado)

Configure ambos os ambientes (dev e prod) de uma vez:

```bash
cd terraform
chmod +x setup-multi-env.sh
./setup-multi-env.sh
```

### Opção 2: Setup Ambiente Único

Configure apenas um ambiente:

```bash
cd terraform
chmod +x setup.sh
./setup.sh
```

### Deploy por Ambiente

```bash
# Development
make full-deploy ENV=dev

# Production
make full-deploy ENV=prod
```

**Documentação completa**: [terraform/MULTI_ENV_GUIDE.md](terraform/MULTI_ENV_GUIDE.md)

## 📁 Estrutura de Arquivos

```
terraform/
├── provider.tf              # Configuração do provider OCI
├── variables.tf             # Definição de variáveis
├── network.tf              # VCN, subnets, gateways, security
├── compute.tf              # Instância de computação
├── database.tf             # Autonomous Database
├── cloud-init.yaml         # Configuração inicial da instância
├── outputs.tf              # Outputs do Terraform
├── terraform.tfvars.example # Exemplo de variáveis
├── .gitignore              # Arquivos a ignorar
├── Makefile                # Comandos úteis
├── setup.sh                # Script de setup automatizado
├── README.md               # Documentação completa
├── QUICKSTART.md           # Guia rápido
└── ARCHITECTURE.md         # Arquitetura detalhada
```

## 🛠️ Comandos Úteis

### Comandos por Ambiente

Todos os comandos aceitam `ENV=dev` ou `ENV=prod`:

```bash
# Ver ajuda
make help

# Listar ambientes
make list-envs

# Development
make init ENV=dev
make plan ENV=dev
make apply ENV=dev
make ssh ENV=dev
make logs ENV=dev

# Production
make init ENV=prod
make plan ENV=prod
make apply ENV=prod
make ssh ENV=prod
make logs ENV=prod

# Outros comandos
make output ENV=<env>     # Ver outputs
make deploy ENV=<env>     # Deploy da aplicação
make restart ENV=<env>    # Reiniciar
make health ENV=<env>     # Verificar saúde
make destroy ENV=<env>    # Destruir infraestrutura
```

## 📊 Arquitetura

```
Internet
   │
   ├─► Internet Gateway
   │        │
   │   Public Subnet (10.0.1.0/24)
   │        │
   │   ┌────┴────┐
   │   │ Compute │ ◄─── SSH (22)
   │   │Instance │ ◄─── HTTP (80)
   │   └────┬────┘ ◄─── HTTPS (443)
   │        │
   │        │ Docker Containers:
   │        ├─► Frontend (Nginx:80)
   │        └─► Backend (Spring:8080)
   │             │
   ├─► NAT Gateway
   │        │
   │   Private Subnet (10.0.2.0/24)
   │        │
   │   ┌────┴────────┐
   │   │ Autonomous  │
   │   │  Database   │
   │   │  (Oracle)   │
   │   └─────────────┘
   │
   └─► Service Gateway
```

## 🔐 Segurança

### Credenciais Necessárias

1. **OCI API Key**: Gerada automaticamente pelo `setup.sh`
2. **SSH Key**: Gerada automaticamente pelo `setup.sh`
3. **Database Passwords**: Solicitadas durante o setup
4. **JWT Secret**: Gerado automaticamente
5. **Encryption Key**: Gerado automaticamente

### Boas Práticas

- ✅ Nunca commite `terraform.tfvars`
- ✅ Use senhas fortes (mínimo 12 caracteres)
- ✅ Habilite MFA na conta OCI
- ✅ Rotacione senhas regularmente
- ✅ Mantenha sistema atualizado

## 📝 Obtendo Credenciais OCI

### Tenancy OCID
1. Console OCI → Menu → Administration → Tenancy Details
2. Copie o OCID

### User OCID
1. Console OCI → Menu → Identity & Security → Users
2. Clique no seu usuário
3. Copie o OCID

### Compartment OCID
1. Console OCI → Menu → Identity & Security → Compartments
2. Selecione ou crie um compartment
3. Copie o OCID

### Região
Exemplos:
- São Paulo: `sa-saopaulo-1`
- Vinhedo: `sa-vinhedo-1`
- Santiago: `sa-santiago-1`

Lista completa: https://docs.oracle.com/en-us/iaas/Content/General/Concepts/regions.htm

## 🔧 Configuração Pós-Deploy

### 1. Configurar Wallet do Banco de Dados

```bash
# Copiar wallet para a instância
scp -i ~/.ssh/oci_key wallet.zip opc@<IP_PUBLICO>:/opt/gestao-financeira/wallet/

# Extrair wallet
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
cd /opt/gestao-financeira/wallet
unzip wallet.zip
chmod 600 *
```

### 2. Deploy da Aplicação

```bash
# Editar script com URL do seu repositório
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
sudo nano /opt/gestao-financeira/deploy.sh

# Executar deploy
sudo /opt/gestao-financeira/deploy.sh
```

### 3. Configurar Domínio (Opcional)

Se você tem um domínio, aponte-o para o IP público:

```
A record: @ -> <IP_PUBLICO>
A record: www -> <IP_PUBLICO>
```

### 4. Configurar HTTPS (Recomendado)

```bash
# Instalar Certbot
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
sudo yum install -y certbot python3-certbot-nginx

# Obter certificado
sudo certbot --nginx -d seudominio.com -d www.seudominio.com

# Renovação automática
sudo systemctl enable certbot-renew.timer
```

## 📈 Monitoramento

### Métricas no Console OCI

**Compute**:
- Compute → Instances → Sua instância → Metrics

**Database**:
- Database → Autonomous Database → Seu DB → Performance Hub

### Logs da Aplicação

```bash
# Logs em tempo real
make logs

# Ou via SSH
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml logs -f
```

### Health Checks

```bash
# Via Makefile
make health

# Ou diretamente
curl http://<IP_PUBLICO>:8080/actuator/health
```

## 🔄 Atualizações

### Atualizar Aplicação

```bash
# Via Makefile
make update

# Ou manualmente
make deploy
make restart
```

### Atualizar Sistema Operacional

```bash
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
sudo yum update -y
sudo reboot
```

### Atualizar Infraestrutura

```bash
# Editar arquivos .tf conforme necessário
terraform plan
terraform apply
```

## 💾 Backup e Recuperação

### Autonomous Database

**Backups Automáticos**:
- Diários, retidos por 60 dias
- Recuperação point-in-time
- Sem custo adicional

**Backup Manual**:
- Console OCI → Database → Autonomous Database → More Actions → Create Manual Backup

### Compute Instance

**Boot Volume Backup**:
- Policy: Bronze (backup semanal)
- Retenção: 4 semanas
- Restauração via console

## 🐛 Troubleshooting

### Erro: "Service limit exceeded"

Você atingiu o limite Always Free. Verifique:
```bash
oci limits resource-availability get --compartment-id <COMPARTMENT_OCID> --service-name compute
```

### Erro: "Out of host capacity"

A região está sem capacidade. Tente:
1. Mudar para outra região
2. Usar VM.Standard.A1.Flex (ARM)
3. Aguardar e tentar novamente

### Instância não responde

```bash
# Verificar console serial no OCI
# Compute → Instances → Sua instância → Console Connection

# Ou recriar instância
terraform taint oci_core_instance.app
terraform apply
```

### Problemas de conexão com banco

```bash
# Testar conectividade
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
telnet <DB_HOST> 1521

# Verificar wallet
ls -la /opt/gestao-financeira/wallet/
```

## 💰 Custos

### Always Free (Atual)

**Custo: R$ 0,00/mês**

Recursos incluídos:
- 2x VM.Standard.E2.1.Micro
- 2x Autonomous Database (1 OCPU, 20GB cada)
- 200GB Block Storage
- 10GB Object Storage
- 10TB outbound data transfer/mês

### Upgrade (Opcional)

Se precisar escalar:

**Compute**:
- VM.Standard.E2.1: ~$36/mês
- VM.Standard.E4.Flex (1 OCPU): ~$22/mês

**Database**:
- Autonomous Database (1 OCPU): ~$216/mês
- Storage adicional: ~$0.025/GB/mês

**Load Balancer**:
- 100 Mbps: ~$10/mês
- 400 Mbps: ~$30/mês

## 📚 Documentação

- **README.md**: Documentação completa e detalhada
- **QUICKSTART.md**: Guia rápido de início
- **ARCHITECTURE.md**: Arquitetura detalhada da infraestrutura
- **terraform.tfvars.example**: Exemplo de configuração

## 🔗 Links Úteis

- [OCI Always Free](https://www.oracle.com/cloud/free/)
- [OCI Documentation](https://docs.oracle.com/en-us/iaas/)
- [Terraform OCI Provider](https://registry.terraform.io/providers/oracle/oci/)
- [Autonomous Database Docs](https://docs.oracle.com/en/cloud/paas/autonomous-database/)

## 🆘 Suporte

Para problemas ou dúvidas:

1. Consulte a documentação em `terraform/README.md`
2. Verifique a seção de troubleshooting
3. Consulte os logs da aplicação
4. Abra uma issue no repositório

## 📄 Licença

Este projeto está sob a licença MIT.

---

**Nota**: Esta configuração usa apenas recursos Always Free da OCI, garantindo custo zero permanente. Não há necessidade de cartão de crédito após o período trial.

Feito com ❤️ para a comunidade
