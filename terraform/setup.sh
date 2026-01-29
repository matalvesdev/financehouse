#!/bin/bash

# Script de setup automatizado para OCI Terraform
# Este script ajuda a configurar as credenciais e variáveis necessárias

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OCI_DIR="$HOME/.oci"
SSH_DIR="$HOME/.ssh"

echo "=========================================="
echo "Setup Terraform - Oracle Cloud (OCI)"
echo "=========================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para imprimir mensagens coloridas
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}!${NC} $1"
}

print_info() {
    echo -e "${NC}→${NC} $1"
}

# Verificar se Terraform está instalado
if ! command -v terraform &> /dev/null; then
    print_error "Terraform não está instalado"
    echo ""
    echo "Instale o Terraform:"
    echo "  macOS: brew install terraform"
    echo "  Linux: https://www.terraform.io/downloads"
    echo "  Windows: choco install terraform"
    exit 1
fi
print_success "Terraform instalado: $(terraform version -json | grep -o '"version":"[^"]*' | cut -d'"' -f4)"

# Criar diretório .oci se não existir
if [ ! -d "$OCI_DIR" ]; then
    mkdir -p "$OCI_DIR"
    chmod 700 "$OCI_DIR"
    print_success "Diretório $OCI_DIR criado"
else
    print_success "Diretório $OCI_DIR já existe"
fi

# Criar diretório .ssh se não existir
if [ ! -d "$SSH_DIR" ]; then
    mkdir -p "$SSH_DIR"
    chmod 700 "$SSH_DIR"
    print_success "Diretório $SSH_DIR criado"
else
    print_success "Diretório $SSH_DIR já existe"
fi

echo ""
echo "=========================================="
echo "1. Configuração de Chaves"
echo "=========================================="
echo ""

# Gerar chave API OCI se não existir
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

# Obter fingerprint
FINGERPRINT=$(openssl rsa -pubout -outform DER -in "$OCI_DIR/oci_api_key.pem" 2>/dev/null | openssl md5 -c | cut -d'=' -f2 | tr -d ' ')
print_success "Fingerprint: $FINGERPRINT"

# Gerar chave SSH se não existir
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
echo "2. Configuração de Credenciais OCI"
echo "=========================================="
echo ""

print_info "Obtenha as seguintes informações no console OCI:"
echo ""

# Solicitar informações do usuário
read -p "Tenancy OCID: " TENANCY_OCID
read -p "User OCID: " USER_OCID
read -p "Compartment OCID (ou deixe vazio para usar o tenancy): " COMPARTMENT_OCID
read -p "Região (ex: sa-saopaulo-1): " REGION

# Usar tenancy como compartment se não fornecido
if [ -z "$COMPARTMENT_OCID" ]; then
    COMPARTMENT_OCID="$TENANCY_OCID"
fi

echo ""
echo "=========================================="
echo "3. Configuração do Banco de Dados"
echo "=========================================="
echo ""

read -sp "Senha do Admin do Banco (mínimo 12 caracteres, com maiúsculas, minúsculas e números): " DB_ADMIN_PASSWORD
echo ""
read -sp "Confirme a senha do Admin: " DB_ADMIN_PASSWORD_CONFIRM
echo ""

if [ "$DB_ADMIN_PASSWORD" != "$DB_ADMIN_PASSWORD_CONFIRM" ]; then
    print_error "As senhas não coincidem"
    exit 1
fi

read -p "Nome do banco de dados (padrão: gestaofinanceira): " DB_NAME
DB_NAME=${DB_NAME:-gestaofinanceira}

read -p "Nome do usuário do banco (padrão: gestao_user): " DB_USERNAME
DB_USERNAME=${DB_USERNAME:-gestao_user}

read -sp "Senha do usuário do banco: " DB_PASSWORD
echo ""

echo ""
echo "=========================================="
echo "4. Configuração da Aplicação"
echo "=========================================="
echo ""

# Gerar secrets
print_info "Gerando secrets da aplicação..."
JWT_SECRET=$(openssl rand -base64 32)
ENCRYPTION_KEY=$(openssl rand -base64 32)
print_success "Secrets gerados"

read -p "Nome da aplicação (padrão: gestao-financeira): " APP_NAME
APP_NAME=${APP_NAME:-gestao-financeira}

read -p "Ambiente (padrão: production): " ENVIRONMENT
ENVIRONMENT=${ENVIRONMENT:-production}

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
    read -p "Número de OCPUs (1-4, padrão: 4): " INSTANCE_OCPUS
    INSTANCE_OCPUS=${INSTANCE_OCPUS:-4}
    read -p "Memória em GB (6-24, padrão: 24): " INSTANCE_MEMORY
    INSTANCE_MEMORY=${INSTANCE_MEMORY:-24}
else
    INSTANCE_SHAPE="VM.Standard.E2.1.Micro"
    INSTANCE_OCPUS=1
    INSTANCE_MEMORY=1
fi

echo ""
echo "=========================================="
echo "6. Criando terraform.tfvars"
echo "=========================================="
echo ""

# Criar arquivo terraform.tfvars
cat > "$SCRIPT_DIR/terraform.tfvars" <<EOF
# OCI Authentication
tenancy_ocid     = "$TENANCY_OCID"
user_ocid        = "$USER_OCID"
fingerprint      = "$FINGERPRINT"
private_key_path = "$OCI_DIR/oci_api_key.pem"
region           = "$REGION"
compartment_ocid = "$COMPARTMENT_OCID"

# Network Configuration
vcn_cidr_block       = "10.0.0.0/16"
public_subnet_cidr   = "10.0.1.0/24"
private_subnet_cidr  = "10.0.2.0/24"

# Compute Configuration
instance_shape          = "$INSTANCE_SHAPE"
EOF

if [ "$INSTANCE_SHAPE" = "VM.Standard.A1.Flex" ]; then
    cat >> "$SCRIPT_DIR/terraform.tfvars" <<EOF
instance_ocpus          = $INSTANCE_OCPUS
instance_memory_in_gbs  = $INSTANCE_MEMORY
EOF
fi

cat >> "$SCRIPT_DIR/terraform.tfvars" <<EOF

# SSH Key
ssh_public_key = "$SSH_PUBLIC_KEY"

# Database Configuration
db_admin_password = "$DB_ADMIN_PASSWORD"
db_name           = "$DB_NAME"
db_username       = "$DB_USERNAME"
db_password       = "$DB_PASSWORD"

# Application Configuration
app_name       = "$APP_NAME"
environment    = "$ENVIRONMENT"
jwt_secret     = "$JWT_SECRET"
encryption_key = "$ENCRYPTION_KEY"

# Tags
tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "$ENVIRONMENT"
}
EOF

chmod 600 "$SCRIPT_DIR/terraform.tfvars"
print_success "Arquivo terraform.tfvars criado"

echo ""
echo "=========================================="
echo "Setup Concluído!"
echo "=========================================="
echo ""
print_success "Configuração concluída com sucesso!"
echo ""
echo "Próximos passos:"
echo ""
echo "  1. Inicializar Terraform:"
echo "     cd $SCRIPT_DIR"
echo "     terraform init"
echo ""
echo "  2. Validar configuração:"
echo "     terraform validate"
echo ""
echo "  3. Ver plano de execução:"
echo "     terraform plan"
echo ""
echo "  4. Aplicar configuração:"
echo "     terraform apply"
echo ""
echo "Ou use o Makefile:"
echo "  make full-deploy"
echo ""
print_warning "IMPORTANTE: Mantenha o arquivo terraform.tfvars seguro e nunca o commite no Git!"
echo ""
