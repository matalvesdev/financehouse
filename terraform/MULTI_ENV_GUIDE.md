# Guia de Múltiplos Ambientes

Este guia explica como gerenciar ambientes separados de desenvolvimento e produção na OCI.

## 📋 Visão Geral

A infraestrutura suporta dois ambientes isolados:

- **Development (dev)**: Para testes e desenvolvimento
- **Production (prod)**: Para aplicação em produção

Cada ambiente tem:
- ✅ Recursos OCI isolados (VCN, instância, banco)
- ✅ Configurações independentes
- ✅ Credenciais separadas
- ✅ CIDRs diferentes para evitar conflitos

## 🏗️ Estrutura de Diretórios

```
terraform/
├── provider.tf
├── variables.tf
├── network.tf
├── compute.tf
├── database.tf
├── outputs.tf
├── cloud-init.yaml
├── Makefile
├── setup-multi-env.sh
└── environments/
    ├── dev/
    │   ├── terraform.tfvars.example
    │   ├── terraform.tfvars (criado pelo setup)
    │   └── .gitignore
    └── prod/
        ├── terraform.tfvars.example
        ├── terraform.tfvars (criado pelo setup)
        └── .gitignore
```

## 🚀 Setup Inicial

### Opção 1: Setup Automatizado (Recomendado)

Configure ambos os ambientes de uma vez:

```bash
cd terraform
chmod +x setup-multi-env.sh
./setup-multi-env.sh
```

O script irá:
1. Gerar chaves API e SSH (compartilhadas)
2. Solicitar credenciais OCI (compartilhadas)
3. Configurar ambiente DEV
4. Configurar ambiente PROD
5. Criar arquivos `terraform.tfvars` para cada ambiente

### Opção 2: Setup Manual por Ambiente

Configure cada ambiente separadamente:

```bash
# Development
make setup ENV=dev
nano environments/dev/terraform.tfvars

# Production
make setup ENV=prod
nano environments/prod/terraform.tfvars
```

## 🎯 Comandos por Ambiente

Todos os comandos aceitam o parâmetro `ENV`:

```bash
# Sintaxe
make <comando> ENV=<dev|prod>

# Exemplos
make plan ENV=dev
make apply ENV=prod
make destroy ENV=dev
```

### Comandos Disponíveis

```bash
# Ver ajuda
make help

# Listar ambientes
make list-envs

# Inicializar
make init ENV=dev
make init ENV=prod

# Planejar
make plan ENV=dev
make plan ENV=prod

# Aplicar
make apply ENV=dev
make apply ENV=prod

# Deploy completo
make full-deploy ENV=dev
make full-deploy ENV=prod

# Ver outputs
make output ENV=dev
make output ENV=prod

# SSH
make ssh ENV=dev
make ssh ENV=prod

# Logs
make logs ENV=dev
make logs ENV=prod

# Destruir
make destroy ENV=dev
make destroy ENV=prod
```

## 📊 Diferenças entre Ambientes

### Development

**Network**:
- VCN: 10.0.0.0/16
- Public Subnet: 10.0.1.0/24
- Private Subnet: 10.0.2.0/24

**Compute**:
- Shape: VM.Standard.E2.1.Micro (ou A1.Flex com 2 OCPUs)
- Nome: gestao-financeira-dev

**Database**:
- Nome: gestaofinanceiradev
- Usuário: gestao_dev

**Application**:
- Logs: DEBUG level
- Senhas: Menos restritivas
- JWT: Secrets de desenvolvimento

### Production

**Network**:
- VCN: 10.1.0.0/16
- Public Subnet: 10.1.1.0/24
- Private Subnet: 10.1.2.0/24

**Compute**:
- Shape: VM.Standard.E2.1.Micro (ou A1.Flex com 4 OCPUs)
- Nome: gestao-financeira

**Database**:
- Nome: gestaofinanceira
- Usuário: gestao_prod

**Application**:
- Logs: INFO level
- Senhas: Fortes e únicas
- JWT: Secrets de produção

## 🔄 Workflow Recomendado

### 1. Desenvolvimento

```bash
# Deploy inicial
make full-deploy ENV=dev

# Desenvolver e testar
make ssh ENV=dev
# ... fazer alterações ...

# Atualizar aplicação
make update ENV=dev

# Ver logs
make logs ENV=dev

# Verificar saúde
make health ENV=dev
```

### 2. Testes em Dev

```bash
# Executar testes
make ssh ENV=dev
cd /opt/gestao-financeira-dev/app
docker-compose -f docker-compose.prod.yml exec backend mvn test

# Verificar métricas
curl http://<DEV_IP>:8080/actuator/metrics
```

### 3. Deploy em Produção

```bash
# Revisar mudanças
make plan ENV=prod

# Deploy
make apply ENV=prod

# Verificar
make health ENV=prod

# Monitorar logs
make logs ENV=prod
```

## 🔐 Segurança

### Credenciais Separadas

**IMPORTANTE**: Use credenciais DIFERENTES para cada ambiente:

```bash
# DEV
DB_ADMIN_PASSWORD: DevPassword123!@#
JWT_SECRET: dev-jwt-secret-...
ENCRYPTION_KEY: dev-encryption-key-...

# PROD
DB_ADMIN_PASSWORD: ProdStrongPassword123!@#$%
JWT_SECRET: STRONG-RANDOM-SECRET-...
ENCRYPTION_KEY: STRONG-RANDOM-KEY-...
```

### Chaves Compartilhadas

Estas chaves são compartilhadas entre ambientes:
- Chave API OCI (~/.oci/oci_api_key.pem)
- Chave SSH (~/.ssh/oci_key)

### Isolamento de Rede

Cada ambiente tem sua própria VCN com CIDRs diferentes:
- DEV: 10.0.0.0/16
- PROD: 10.1.0.0/16

Isso garante isolamento completo de rede.

## 📝 Gerenciamento de State

### State Files Separados

Cada ambiente mantém seu próprio state file:

```
terraform/
├── terraform.tfstate (dev ou prod, dependendo do último comando)
└── environments/
    ├── dev/
    │   └── terraform.tfstate (se usar backend local)
    └── prod/
        └── terraform.tfstate (se usar backend local)
```

### Backend Remoto (Recomendado para Prod)

Para produção, use backend remoto (S3, OCI Object Storage, etc.):

```hcl
# backend.tf
terraform {
  backend "s3" {
    bucket = "terraform-state-prod"
    key    = "gestao-financeira/terraform.tfstate"
    region = "sa-saopaulo-1"
  }
}
```

## 🔄 Promoção de Configurações

### Comparar Ambientes

```bash
# Ver diferenças
make compare-envs

# Ou manualmente
diff -u environments/dev/terraform.tfvars environments/prod/terraform.tfvars
```

### Promover para Produção

**CUIDADO**: Revise cuidadosamente antes de promover!

```bash
# Criar backup
cp environments/prod/terraform.tfvars environments/prod/terraform.tfvars.backup

# Revisar diferenças
make compare-envs

# Aplicar mudanças manualmente
nano environments/prod/terraform.tfvars

# Testar
make plan ENV=prod

# Aplicar
make apply ENV=prod
```

## 🐛 Troubleshooting

### Erro: "terraform.tfvars not found"

```bash
# Verificar se arquivo existe
ls -la environments/dev/terraform.tfvars
ls -la environments/prod/terraform.tfvars

# Criar se necessário
make setup ENV=dev
make setup ENV=prod
```

### Conflito de Recursos

Se tentar criar recursos duplicados:

```bash
# Verificar recursos existentes
make output ENV=dev
make output ENV=prod

# Destruir ambiente se necessário
make destroy ENV=dev
```

### Erro de Autenticação

```bash
# Verificar chave API
ls -la ~/.oci/oci_api_key.pem

# Verificar fingerprint
openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem | openssl md5 -c

# Verificar no console OCI
# Identity → Users → Seu usuário → API Keys
```

## 📊 Monitoramento Multi-Ambiente

### Métricas por Ambiente

```bash
# DEV
curl http://<DEV_IP>:8080/actuator/health
curl http://<DEV_IP>:8080/actuator/metrics

# PROD
curl http://<PROD_IP>:8080/actuator/health
curl http://<PROD_IP>:8080/actuator/metrics
```

### Logs por Ambiente

```bash
# DEV
make logs ENV=dev

# PROD
make logs ENV=prod
```

### Alertas

Configure alertas separados no console OCI para cada ambiente:

**DEV**:
- CPU > 90% (menos crítico)
- Memory > 95%

**PROD**:
- CPU > 80% (mais crítico)
- Memory > 90%
- Health check failures

## 💰 Custos

### Always Free (Ambos os Ambientes)

Com recursos Always Free, você pode ter:
- ✅ 2x VM.Standard.E2.1.Micro (1 para dev, 1 para prod)
- ✅ 2x Autonomous Database (1 para dev, 1 para prod)
- ✅ **Custo Total: R$ 0,00/mês**

### Alternativa ARM

Se usar VM.Standard.A1.Flex:
- ✅ Total de 4 OCPUs compartilhados
- ✅ Total de 24GB RAM compartilhados
- Exemplo: 2 OCPUs para dev + 2 OCPUs para prod
- ✅ **Custo Total: R$ 0,00/mês**

## 🔄 CI/CD Multi-Ambiente

### GitHub Actions

Crie workflows separados:

**.github/workflows/deploy-dev.yml**:
```yaml
name: Deploy to Development

on:
  push:
    branches: [ develop ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to Dev
        run: |
          cd terraform
          make apply ENV=dev
        env:
          TF_VAR_file: environments/dev/terraform.tfvars
```

**.github/workflows/deploy-prod.yml**:
```yaml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production  # Requer aprovação
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to Prod
        run: |
          cd terraform
          make apply ENV=prod
        env:
          TF_VAR_file: environments/prod/terraform.tfvars
```

## 📚 Referências

- [Terraform Workspaces](https://www.terraform.io/docs/language/state/workspaces.html)
- [OCI Always Free](https://www.oracle.com/cloud/free/)
- [Multi-Environment Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices/part1.html)

---

**Dica**: Sempre teste mudanças em DEV antes de aplicar em PROD!
