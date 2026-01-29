#!/bin/bash

# Script de setup para múltiplos ambientes (dev e prod)
# Este script configura ambos os ambientes de uma vez

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OCI_DIR="$HOME/.oci"
SSH_DIR="$HOME/.ssh"

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✓${NC} $1"; }
print_error() { echo -e "${RED}✗${NC} $1"; }
print_warning() { echo -e "${YELLOW}!${NC} $1"; }
print_info() { echo -e "${BLUE}→${NC} $1"; }

echo "=========================================="
echo "Setup Multi-Ambiente - Oracle Cloud (OCI)"
echo "=========================================="
echo ""

# Verificar Terraform
if ! command -v terraform &> /dev/null; then
    print_error "Terraform não está instalado"
    exit 1
fi
print_success "Terraform instalado: $(terraform version -json | grep -o '"version":"[^"]*' | cut -d'"' -f4)"

# Criar diretórios
mkdir -p "$OCI_DIR" "$SSH_DIR"
chmod 700 "$OCI_DIR" "$SSH_DIR"

echo ""
echo "=========================================="
echo "1. Configuração de Chaves"
echo "=========================================="
echo ""

# Gerar chave API OCI
if [ ! -f "$OCI_DIR/oci_api_key.pem" ]; then
    print_info "Gerando chave API OCI..."
    openssl genrsa -out "$OCI_DIR/oci_api_key.pem" 2048
    chmod 600 "$OCI_DIR/oci_api_key.pem"
    openssl rsa -pubout -in "$OCI_DIR/oci_api_key.pem" -out "$OCI_DIR/oci_api_key_public.pem"
    print_success "Chave API OCI gerada"
    
    echo ""
    print_warning "IMPORTANTE: Adicione a chave pública no console OCI:"
    echo "  1. Acesse: Identity → Users → Seu usuário → API Keys"
    echo "  2. Clique em 'Add API Key'"
    echo "  3. Selecione 'Paste Public Key'"
    echo "  4. Cole o conteúdo abaixo:"
    echo ""
    cat "$OCI_DIR/oci_api_key_public.pem"
    echo ""
    read -p "Pressione ENTER após adicionar a chave no console OCI..."
else
    print_success "Chave API OCI já existe"
fi

FINGERPRINT=$(openssl rsa -pubout -outform DER -in "$OCI_DIR/oci_api_key.pem" 2>/dev/null | openssl md5 -c | cut -d'=' -f2 | tr -d ' ')
print_success "Fingerprint: $FINGERPRINT"

# Gerar chave SSH
if [ ! -f "$SSH_DIR/oci_key" ]; then
    print_info "Gerando chave SSH..."
    ssh-keygen -t rsa -b 4096 -f "$SSH_DIR/oci_key" -N "" -C "oci-instance-key"
    chmod 600 "$SSH_DIR/oci_key"
    chmod 644 "$SSH_DIR/oci_key.pub"
    print_success "Chave SSH gerada"
else
    print_success "Chave SSH já existe"
fi

SSH_PUBLIC_KEY=$(cat "$SSH_DIR/oci_key.pub")

echo ""
echo "=========================================="
echo "2. Credenciais OCI (Compartilhadas)"
echo "=========================================="
echo ""

read -p "Tenancy OCID: " TENANCY_OCID
read -p "User OCID: " USER_OCID
read -p "Compartment OCID (ou deixe vazio para usar o tenancy): " COMPARTMENT_OCID
read -p "Região (ex: sa-saopaulo-1): " REGION

if [ -z "$COMPARTMENT_OCID" ]; then
    COMPARTMENT_OCID="$TENANCY_OCID"
fi

echo ""
echo "=========================================="
echo "3. Configuração DESENVOLVIMENTO"
echo "=========================================="
echo ""

print_info "Configurando ambiente de desenvolvimento..."

read -sp "Senha do Admin do Banco DEV (mínimo 12 caracteres): " DEV_DB_ADMIN_PASSWORD
echo ""
read -sp "Confirme a senha: " DEV_DB_ADMIN_PASSWORD_CONFIRM
echo ""

if [ "$DEV_DB_ADMIN_PASSWORD" != "$DEV_DB_ADMIN_PASSWORD_CONFIRM" ]; then
    print_error "As senhas não coincidem"
    exit 1
fi

read -p "Nome do banco DEV (padrão: gestaofinanceiradev): " DEV_DB_NAME
DEV_DB_NAME=${DEV_DB_NAME:-gestaofinanceiradev}

read -p "Nome do usuário do banco DEV (padrão: gestao_dev): " DEV_DB_USERNAME
DEV_DB_USERNAME=${DEV_DB_USERNAME:-gestao_dev}

read -sp "Senha do usuário do banco DEV: " DEV_DB_PASSWORD
echo ""

print_info "Gerando secrets para DEV..."
DEV_JWT_SECRET=$(openssl rand -base64 32)
DEV_ENCRYPTION_KEY=$(openssl rand -base64 32)
print_success "Secrets DEV gerados"

echo ""
echo "=========================================="
echo "4. Configuração PRODUÇÃO"
echo "=========================================="
echo ""

print_warning "IMPORTANTE: Use senhas DIFERENTES e FORTES para produção!"
echo ""

read -sp "Senha do Admin do Banco PROD (mínimo 12 caracteres): " PROD_DB_ADMIN_PASSWORD
echo ""
read -sp "Confirme a senha: " PROD_DB_ADMIN_PASSWORD_CONFIRM
echo ""

if [ "$PROD_DB_ADMIN_PASSWORD" != "$PROD_DB_ADMIN_PASSWORD_CONFIRM" ]; then
    print_error "As senhas não coincidem"
    exit 1
fi

read -p "Nome do banco PROD (padrão: gestaofinanceira): " PROD_DB_NAME
PROD_DB_NAME=${PROD_DB_NAME:-gestaofinanceira}

read -p "Nome do usuário do banco PROD (padrão: gestao_prod): " PROD_DB_USERNAME
PROD_DB_USERNAME=${PROD_DB_USERNAME:-gestao_prod}

read -sp "Senha do usuário do banco PROD: " PROD_DB_PASSWORD
echo ""

print_info "Gerando secrets para PROD..."
PROD_JWT_SECRET=$(openssl rand -base64 32)
PROD_ENCRYPTION_KEY=$(openssl rand -base64 32)
print_success "Secrets PROD gerados"

echo ""
echo "=========================================="
echo "5. Configuração de Compute"
echo "=========================================="
echo ""

echo "Escolha o tipo de instância:"
echo "  1) VM.Standard.E2.1.Micro (AMD x86, 1 OCPU, 1GB RAM) - Recomendado"
echo "  2) VM.Standard.A1.Flex (ARM, até 4 OCPUs, 24GB RAM)"
read -p "Opção [1]: " INSTANCE_CHOICE
INSTANCE_CHOICE=${INSTANCE_CHOICE:-1}

if [ "$INSTANCE_CHOICE" = "2" ]; then
    INSTANCE_SHAPE="VM.Standard.A1.Flex"
    
    echo ""
    echo "Configuração para DEV:"
    read -p "  OCPUs DEV (1-4, padrão: 2): " DEV_INSTANCE_OCPUS
    DEV_INSTANCE_OCPUS=${DEV_INSTANCE_OCPUS:-2}
    read -p "  Memória DEV em GB (6-24, padrão: 12): " DEV_INSTANCE_MEMORY
    DEV_INSTANCE_MEMORY=${DEV_INSTANCE_MEMORY:-12}
    
    echo ""
    echo "Configuração para PROD:"
    read -p "  OCPUs PROD (1-4, padrão: 4): " PROD_INSTANCE_OCPUS
    PROD_INSTANCE_OCPUS=${PROD_INSTANCE_OCPUS:-4}
    read -p "  Memória PROD em GB (6-24, padrão: 24): " PROD_INSTANCE_MEMORY
    PROD_INSTANCE_MEMORY=${PROD_INSTANCE_MEMORY:-24}
else
    INSTANCE_SHAPE="VM.Standard.E2.1.Micro"
    DEV_INSTANCE_OCPUS=1
    DEV_INSTANCE_MEMORY=1
    PROD_INSTANCE_OCPUS=1
    PROD_INSTANCE_MEMORY=1
fi

echo ""
echo "=========================================="
echo "6. Criando Arquivos de Configuração"
echo "=========================================="
echo ""

# Criar terraform.tfvars para DEV
print_info "Criando environments/dev/terraform.tfvars..."
cat > "$SCRIPT_DIR/environments/dev/terraform.tfvars" <<EOF
# OCI Authentication
tenancy_ocid     = "$TENANCY_OCID"
user_ocid        = "$USER_OCID"
fingerprint      = "$FINGERPRINT"
private_key_path = "$OCI_DIR/oci_api_key.pem"
region           = "$REGION"
compartment_ocid = "$COMPARTMENT_OCID"

# Environment
environment = "development"

# Network Configuration
vcn_cidr_block       = "10.0.0.0/16"
public_subnet_cidr   = "10.0.1.0/24"
private_subnet_cidr  = "10.0.2.0/24"

# Compute Configuration
instance_shape          = "$INSTANCE_SHAPE"
EOF

if [ "$INSTANCE_SHAPE" = "VM.Standard.A1.Flex" ]; then
    cat >> "$SCRIPT_DIR/environments/dev/terraform.tfvars" <<EOF
instance_ocpus          = $DEV_INSTANCE_OCPUS
instance_memory_in_gbs  = $DEV_INSTANCE_MEMORY
EOF
fi

cat >> "$SCRIPT_DIR/environments/dev/terraform.tfvars" <<EOF

# SSH Key
ssh_public_key = "$SSH_PUBLIC_KEY"

# Database Configuration
db_admin_password = "$DEV_DB_ADMIN_PASSWORD"
db_name           = "$DEV_DB_NAME"
db_username       = "$DEV_DB_USERNAME"
db_password       = "$DEV_DB_PASSWORD"

# Application Configuration
app_name       = "gestao-financeira-dev"
jwt_secret     = "$DEV_JWT_SECRET"
encryption_key = "$DEV_ENCRYPTION_KEY"

# Tags
tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "Development"
}
EOF

chmod 600 "$SCRIPT_DIR/environments/dev/terraform.tfvars"
print_success "Arquivo DEV criado"

# Criar terraform.tfvars para PROD
print_info "Criando environments/prod/terraform.tfvars..."
cat > "$SCRIPT_DIR/environments/prod/terraform.tfvars" <<EOF
# OCI Authentication
tenancy_ocid     = "$TENANCY_OCID"
user_ocid        = "$USER_OCID"
fingerprint      = "$FINGERPRINT"
private_key_path = "$OCI_DIR/oci_api_key.pem"
region           = "$REGION"
compartment_ocid = "$COMPARTMENT_OCID"

# Environment
environment = "production"

# Network Configuration
vcn_cidr_block       = "10.1.0.0/16"
public_subnet_cidr   = "10.1.1.0/24"
private_subnet_cidr  = "10.1.2.0/24"

# Compute Configuration
instance_shape          = "$INSTANCE_SHAPE"
EOF

if [ "$INSTANCE_SHAPE" = "VM.Standard.A1.Flex" ]; then
    cat >> "$SCRIPT_DIR/environments/prod/terraform.tfvars" <<EOF
instance_ocpus          = $PROD_INSTANCE_OCPUS
instance_memory_in_gbs  = $PROD_INSTANCE_MEMORY
EOF
fi

cat >> "$SCRIPT_DIR/environments/prod/terraform.tfvars" <<EOF

# SSH Key
ssh_public_key = "$SSH_PUBLIC_KEY"

# Database Configuration
db_admin_password = "$PROD_DB_ADMIN_PASSWORD"
db_name           = "$PROD_DB_NAME"
db_username       = "$PROD_DB_USERNAME"
db_password       = "$PROD_DB_PASSWORD"

# Application Configuration
app_name       = "gestao-financeira"
jwt_secret     = "$PROD_JWT_SECRET"
encryption_key = "$PROD_ENCRYPTION_KEY"

# Tags
tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "Production"
  Backup      = "Required"
}
EOF

chmod 600 "$SCRIPT_DIR/environments/prod/terraform.tfvars"
print_success "Arquivo PROD criado"

echo ""
echo "=========================================="
echo "Setup Concluído!"
echo "=========================================="
echo ""
print_success "Configuração de ambientes concluída com sucesso!"
echo ""
echo "Próximos passos:"
echo ""
echo "  DESENVOLVIMENTO:"
echo "    cd $SCRIPT_DIR"
echo "    make init ENV=dev"
echo "    make plan ENV=dev"
echo "    make apply ENV=dev"
echo ""
echo "  PRODUÇÃO:"
echo "    cd $SCRIPT_DIR"
echo "    make init ENV=prod"
echo "    make plan ENV=prod"
echo "    make apply ENV=prod"
echo ""
echo "Ou use deploy completo:"
echo "  make full-deploy ENV=dev"
echo "  make full-deploy ENV=prod"
echo ""
print_warning "IMPORTANTE:"
echo "  - Arquivos terraform.tfvars contêm informações sensíveis"
echo "  - Nunca commite esses arquivos no Git"
echo "  - Use senhas diferentes para dev e prod"
echo "  - Habilite MFA na conta OCI"
echo ""
