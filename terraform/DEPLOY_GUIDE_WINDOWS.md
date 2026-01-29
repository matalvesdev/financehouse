# Guia de Deploy para Windows - Oracle Cloud Infrastructure

Este guia mostra como fazer deploy na OCI usando Windows.

## 📋 Pré-requisitos

- ✅ Terraform instalado (você já tem!)
- ✅ Conta OCI criada
- ✅ Git Bash ou PowerShell

## 🚀 Passo a Passo

### 1. Criar Conta OCI (se ainda não tiver)

1. Acesse: https://www.oracle.com/cloud/free/
2. Clique em "Start for free"
3. Preencha o formulário
4. Verifique seu email
5. Complete o cadastro

**Importante**: Você terá $300 de créditos grátis + recursos Always Free permanentes!

### 2. Obter Credenciais OCI

#### 2.1 Tenancy OCID

1. Faça login no console OCI: https://cloud.oracle.com
2. Clique no menu ☰ (canto superior esquerdo)
3. Vá em: **Administration → Tenancy Details**
4. Copie o **OCID** (começa com `ocid1.tenancy.oc1...`)

#### 2.2 User OCID

1. No console OCI, clique no menu ☰
2. Vá em: **Identity & Security → Users**
3. Clique no seu usuário
4. Copie o **OCID** (começa com `ocid1.user.oc1...`)

#### 2.3 Compartment OCID

1. No console OCI, clique no menu ☰
2. Vá em: **Identity & Security → Compartments**
3. Você pode usar o compartment root (mesmo OCID do tenancy)
4. Ou criar um novo compartment e copiar o OCID

#### 2.4 Região

Escolha a região mais próxima:
- **São Paulo**: `sa-saopaulo-1` (recomendado para Brasil)
- **Vinhedo**: `sa-vinhedo-1`
- **Santiago**: `sa-santiago-1`

Lista completa: https://docs.oracle.com/en-us/iaas/Content/General/Concepts/regions.htm

### 3. Executar Setup

Abra o **Git Bash** ou **PowerShell** e execute:

```bash
# Navegar para a pasta do projeto
cd C:\Users\Mateus Alves Bassane\Desktop\financehouse\terraform

# Dar permissão de execução (Git Bash)
chmod +x setup-multi-env.sh

# Executar setup
bash setup-multi-env.sh
```

**OU** se preferir PowerShell:

```powershell
# Navegar para a pasta
cd C:\Users\Mateus Alves Bassane\Desktop\financehouse\terraform

# Executar setup manualmente
# Vamos criar os arquivos passo a passo
```

### 4. Setup Manual (Alternativa)

Se o script não funcionar, você pode fazer manualmente:

#### 4.1 Gerar Chave API OCI

```powershell
# Criar diretório .oci
mkdir $HOME\.oci

# Gerar chave privada (use Git Bash ou WSL)
openssl genrsa -out $HOME\.oci\oci_api_key.pem 2048

# Gerar chave pública
openssl rsa -pubout -in $HOME\.oci\oci_api_key.pem -out $HOME\.oci\oci_api_key_public.pem

# Ver chave pública (copie o conteúdo)
cat $HOME\.oci\oci_api_key_public.pem
```

#### 4.2 Adicionar Chave no Console OCI

1. No console OCI, vá em: **Identity & Security → Users → Seu usuário**
2. Clique em **API Keys** (no menu lateral)
3. Clique em **Add API Key**
4. Selecione **Paste Public Key**
5. Cole o conteúdo da chave pública
6. Clique em **Add**

#### 4.3 Obter Fingerprint

```powershell
# Obter fingerprint da chave
openssl rsa -pubout -outform DER -in $HOME\.oci\oci_api_key.pem | openssl md5 -c
```

Copie o fingerprint (formato: `aa:bb:cc:dd:ee:ff:...`)

#### 4.4 Gerar Chave SSH

```powershell
# Criar diretório .ssh se não existir
mkdir $HOME\.ssh

# Gerar chave SSH
ssh-keygen -t rsa -b 4096 -f $HOME\.ssh\oci_key -N ""

# Ver chave pública SSH
cat $HOME\.ssh\oci_key.pub
```

#### 4.5 Criar Arquivo de Configuração DEV

Crie o arquivo `terraform\environments\dev\terraform.tfvars`:

```hcl
# OCI Authentication
tenancy_ocid     = "ocid1.tenancy.oc1..aaaaaaaa..."  # Cole seu Tenancy OCID
user_ocid        = "ocid1.user.oc1..aaaaaaaa..."     # Cole seu User OCID
fingerprint      = "aa:bb:cc:dd:ee:ff:..."           # Cole seu Fingerprint
private_key_path = "C:/Users/Mateus Alves Bassane/.oci/oci_api_key.pem"
region           = "sa-saopaulo-1"
compartment_ocid = "ocid1.compartment.oc1..aaaaaaaa..." # Cole seu Compartment OCID

# Environment
environment = "development"

# Network Configuration
vcn_cidr_block       = "10.0.0.0/16"
public_subnet_cidr   = "10.0.1.0/24"
private_subnet_cidr  = "10.0.2.0/24"

# Compute Configuration
instance_shape = "VM.Standard.E2.1.Micro"

# SSH Key (cole a chave pública SSH aqui)
ssh_public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC..."

# Database Configuration
db_admin_password = "DevPassword123!@#"
db_name           = "gestaofinanceiradev"
db_username       = "gestao_dev"
db_password       = "DevUserPass123!@#"

# Application Configuration
app_name       = "gestao-financeira-dev"
jwt_secret     = "dev-jwt-secret-change-this-32chars"
encryption_key = "dev-encryption-key-32-chars-here"

# Tags
tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "Development"
}
```

**Importante**: Substitua todos os valores com `...` pelos seus valores reais!

### 5. Inicializar Terraform

```powershell
# Navegar para a pasta terraform
cd terraform

# Inicializar Terraform
terraform init
```

### 6. Validar Configuração

```powershell
# Validar arquivos
terraform validate

# Formatar arquivos
terraform fmt
```

### 7. Ver Plano de Execução

```powershell
# Ver o que será criado
terraform plan -var-file="environments/dev/terraform.tfvars"
```

Revise cuidadosamente o plano. Você deve ver:
- 1 VCN
- 2 Subnets
- 1 Internet Gateway
- 1 NAT Gateway
- 1 Service Gateway
- 1 Compute Instance
- 1 Autonomous Database
- Security Lists e NSGs

### 8. Aplicar Configuração

```powershell
# Criar infraestrutura
terraform apply -var-file="environments/dev/terraform.tfvars"
```

Digite `yes` quando solicitado.

**Aguarde ~10-15 minutos** ☕

### 9. Ver Informações de Deploy

```powershell
# Ver IP público e outras informações
terraform output
```

Você verá:
- IP público da instância
- String de conexão do banco
- Comando SSH
- URL da aplicação

### 10. Conectar à Instância

```powershell
# Conectar via SSH
ssh -i $HOME\.ssh\oci_key opc@<IP_PUBLICO>
```

Substitua `<IP_PUBLICO>` pelo IP mostrado no output.

### 11. Verificar Instalação

Dentro da instância:

```bash
# Verificar Docker
docker --version

# Verificar diretório da aplicação
ls -la /opt/gestao-financeira-dev/

# Ver logs de inicialização
sudo cat /var/log/cloud-init-output.log
```

### 12. Configurar Wallet do Banco

O wallet foi baixado para `terraform/wallet.zip`. Copie para a instância:

```powershell
# Copiar wallet
scp -i $HOME\.ssh\oci_key wallet.zip opc@<IP_PUBLICO>:/tmp/

# Conectar e extrair
ssh -i $HOME\.ssh\oci_key opc@<IP_PUBLICO>
sudo mkdir -p /opt/gestao-financeira-dev/wallet
sudo mv /tmp/wallet.zip /opt/gestao-financeira-dev/wallet/
cd /opt/gestao-financeira-dev/wallet
sudo unzip wallet.zip
sudo chmod 600 *
```

### 13. Deploy da Aplicação

```bash
# Editar script de deploy com URL do repositório
sudo nano /opt/gestao-financeira-dev/deploy.sh

# Alterar a linha:
REPO_URL="https://github.com/matalvesdev/financehouse.git"

# Salvar (Ctrl+O, Enter, Ctrl+X)

# Executar deploy
sudo /opt/gestao-financeira-dev/deploy.sh
```

### 14. Verificar Aplicação

```bash
# Ver logs
docker-compose -f /opt/gestao-financeira-dev/app/docker-compose.prod.yml logs -f

# Verificar status
docker-compose -f /opt/gestao-financeira-dev/app/docker-compose.prod.yml ps

# Testar endpoint
curl http://localhost:8080/actuator/health
```

### 15. Acessar Aplicação

Abra o navegador e acesse:
```
http://<IP_PUBLICO>
```

## 🎉 Pronto!

Sua aplicação está rodando na Oracle Cloud!

## 📊 Próximos Passos

### Deploy em Produção

Repita o processo para produção:

1. Crie `terraform/environments/prod/terraform.tfvars`
2. Use senhas DIFERENTES e FORTES
3. Use CIDR diferente (10.1.0.0/16)
4. Execute:
   ```powershell
   terraform plan -var-file="environments/prod/terraform.tfvars"
   terraform apply -var-file="environments/prod/terraform.tfvars"
   ```

### Configurar Domínio

Se você tem um domínio:

1. Aponte o DNS para o IP público
2. Configure HTTPS com Let's Encrypt:
   ```bash
   sudo yum install -y certbot python3-certbot-nginx
   sudo certbot --nginx -d seudominio.com
   ```

### Monitoramento

Configure alertas no console OCI:
- CPU > 80%
- Memory > 90%
- Database storage > 80%

## 🐛 Troubleshooting

### Erro: "Service limit exceeded"

Você atingiu o limite Always Free. Verifique no console:
- Governance → Limits, Quotas and Usage

### Erro: "Out of host capacity"

A região está sem capacidade. Tente:
1. Outra região
2. VM.Standard.A1.Flex (ARM)
3. Aguarde e tente novamente

### Erro de Autenticação

Verifique:
1. Chave API adicionada no console OCI
2. Fingerprint correto
3. Caminho da chave privada correto

### Instância não responde

```powershell
# Verificar console serial no OCI
# Compute → Instances → Sua instância → Console Connection

# Ou recriar instância
terraform taint oci_core_instance.app
terraform apply -var-file="environments/dev/terraform.tfvars"
```

## 💰 Custos

**Custo Total: R$ 0,00/mês** 🎉

Você está usando apenas recursos Always Free!

## 📚 Documentação

- [MULTI_ENV_GUIDE.md](MULTI_ENV_GUIDE.md) - Guia completo de ambientes
- [README.md](README.md) - Documentação Terraform
- [QUICKSTART.md](QUICKSTART.md) - Início rápido
- [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitetura detalhada

## 🆘 Precisa de Ajuda?

- Documentação OCI: https://docs.oracle.com/en-us/iaas/
- Terraform OCI: https://registry.terraform.io/providers/oracle/oci/
- Suporte OCI: https://support.oracle.com

---

**Boa sorte com o deploy!** 🚀
