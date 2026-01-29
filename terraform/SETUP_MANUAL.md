# Setup Manual - Passo a Passo

Como o script interativo precisa de suas credenciais, siga este guia manual.

## 🔐 Passo 1: Gerar Chaves

### 1.1 Chave API OCI

Abra o **Git Bash** e execute:

```bash
# Criar diretório
mkdir -p ~/.oci
chmod 700 ~/.oci

# Gerar chave privada
openssl genrsa -out ~/.oci/oci_api_key.pem 2048
chmod 600 ~/.oci/oci_api_key.pem

# Gerar chave pública
openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem

# Ver chave pública (COPIE TODO O CONTEÚDO)
cat ~/.oci/oci_api_key_public.pem
```

**Copie a chave pública** (incluindo as linhas BEGIN e END).

### 1.2 Adicionar Chave no Console OCI

1. Acesse: https://cloud.oracle.com
2. Faça login
3. Clique no ícone do usuário (canto superior direito)
4. Clique em **User Settings**
5. No menu lateral, clique em **API Keys**
6. Clique em **Add API Key**
7. Selecione **Paste Public Key**
8. Cole a chave pública que você copiou
9. Clique em **Add**

### 1.3 Obter Fingerprint

No Git Bash:

```bash
# Obter fingerprint
openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem 2>/dev/null | openssl md5 -c | cut -d'=' -f2 | tr -d ' '
```

**Copie o fingerprint** (formato: aa:bb:cc:dd:ee:ff:...)

### 1.4 Gerar Chave SSH

No Git Bash:

```bash
# Criar diretório
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Gerar chave SSH
ssh-keygen -t rsa -b 4096 -f ~/.ssh/oci_key -N ""
chmod 600 ~/.ssh/oci_key
chmod 644 ~/.ssh/oci_key.pub

# Ver chave pública SSH (COPIE TODO O CONTEÚDO)
cat ~/.ssh/oci_key.pub
```

**Copie a chave pública SSH** (uma linha longa começando com ssh-rsa).

## 📋 Passo 2: Obter Credenciais OCI

### 2.1 Tenancy OCID

1. No console OCI, clique no menu ☰ (canto superior esquerdo)
2. Vá em: **Administration → Tenancy Details**
3. Copie o **OCID** (começa com `ocid1.tenancy.oc1...`)

### 2.2 User OCID

1. No console OCI, clique no menu ☰
2. Vá em: **Identity & Security → Users**
3. Clique no seu usuário
4. Copie o **OCID** (começa com `ocid1.user.oc1...`)

### 2.3 Compartment OCID

Opção 1: Usar o compartment root (mais simples)
- Use o mesmo OCID do Tenancy

Opção 2: Criar um compartment específico
1. No console OCI, clique no menu ☰
2. Vá em: **Identity & Security → Compartments**
3. Clique em **Create Compartment**
4. Nome: `gestao-financeira`
5. Clique em **Create Compartment**
6. Copie o **OCID**

### 2.4 Região

Escolha a região mais próxima:
- **São Paulo**: `sa-saopaulo-1` ✅ (recomendado)
- **Vinhedo**: `sa-vinhedo-1`
- **Santiago**: `sa-santiago-1`

## 📝 Passo 3: Criar Arquivos de Configuração

### 3.1 Ambiente de Desenvolvimento

Crie o arquivo: `terraform/environments/dev/terraform.tfvars`

```hcl
# OCI Authentication
tenancy_ocid     = "COLE_SEU_TENANCY_OCID_AQUI"
user_ocid        = "COLE_SEU_USER_OCID_AQUI"
fingerprint      = "COLE_SEU_FINGERPRINT_AQUI"
private_key_path = "C:/Users/Mateus Alves Bassane/.oci/oci_api_key.pem"
region           = "sa-saopaulo-1"
compartment_ocid = "COLE_SEU_COMPARTMENT_OCID_AQUI"

# Environment
environment = "development"

# Network Configuration
vcn_cidr_block       = "10.0.0.0/16"
public_subnet_cidr   = "10.0.1.0/24"
private_subnet_cidr  = "10.0.2.0/24"

# Compute Configuration
instance_shape = "VM.Standard.E2.1.Micro"

# SSH Key
ssh_public_key = "COLE_SUA_CHAVE_SSH_PUBLICA_AQUI"

# Database Configuration
db_admin_password = "DevPassword123!@#"
db_name           = "gestaofinanceiradev"
db_username       = "gestao_dev"
db_password       = "DevUserPass123!@#"

# Application Configuration
app_name       = "gestao-financeira-dev"
jwt_secret     = "dev-jwt-secret-32-chars-change-this"
encryption_key = "dev-encryption-key-32-chars-here"

# Tags
tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "Development"
}
```

**Substitua**:
- `COLE_SEU_TENANCY_OCID_AQUI` → Seu Tenancy OCID
- `COLE_SEU_USER_OCID_AQUI` → Seu User OCID
- `COLE_SEU_FINGERPRINT_AQUI` → Seu Fingerprint
- `COLE_SEU_COMPARTMENT_OCID_AQUI` → Seu Compartment OCID
- `COLE_SUA_CHAVE_SSH_PUBLICA_AQUI` → Sua chave SSH pública

### 3.2 Ambiente de Produção (Opcional)

Se quiser criar produção também, crie: `terraform/environments/prod/terraform.tfvars`

```hcl
# OCI Authentication
tenancy_ocid     = "COLE_SEU_TENANCY_OCID_AQUI"
user_ocid        = "COLE_SEU_USER_OCID_AQUI"
fingerprint      = "COLE_SEU_FINGERPRINT_AQUI"
private_key_path = "C:/Users/Mateus Alves Bassane/.oci/oci_api_key.pem"
region           = "sa-saopaulo-1"
compartment_ocid = "COLE_SEU_COMPARTMENT_OCID_AQUI"

# Environment
environment = "production"

# Network Configuration
vcn_cidr_block       = "10.1.0.0/16"
public_subnet_cidr   = "10.1.1.0/24"
private_subnet_cidr  = "10.1.2.0/24"

# Compute Configuration
instance_shape = "VM.Standard.E2.1.Micro"

# SSH Key
ssh_public_key = "COLE_SUA_CHAVE_SSH_PUBLICA_AQUI"

# Database Configuration
db_admin_password = "ProdStrongPassword123!@#$%"
db_name           = "gestaofinanceira"
db_username       = "gestao_prod"
db_password       = "ProdUserStrongPass123!@#$%"

# Application Configuration
app_name       = "gestao-financeira"
jwt_secret     = "prod-jwt-secret-CHANGE-THIS-32-CHARS"
encryption_key = "prod-encryption-key-CHANGE-32-CHARS"

# Tags
tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "Production"
  Backup      = "Required"
}
```

**IMPORTANTE**: Use senhas DIFERENTES e FORTES para produção!

## ✅ Passo 4: Verificar Arquivos

Verifique se os arquivos foram criados:

```powershell
# Verificar chaves
ls $HOME\.oci\
ls $HOME\.ssh\

# Verificar configuração
cat terraform\environments\dev\terraform.tfvars
```

## 🚀 Passo 5: Inicializar Terraform

```powershell
cd terraform
terraform init
```

## ✅ Passo 6: Validar

```powershell
terraform validate
terraform fmt
```

## 📊 Passo 7: Ver Plano

```powershell
terraform plan -var-file="environments/dev/terraform.tfvars"
```

Revise o plano cuidadosamente!

## 🎯 Passo 8: Aplicar

```powershell
terraform apply -var-file="environments/dev/terraform.tfvars"
```

Digite `yes` quando solicitado.

Aguarde ~10-15 minutos ☕

## 📋 Passo 9: Ver Resultados

```powershell
terraform output
```

Anote:
- IP público: ___________________________
- Connection string: ___________________________

## 🎉 Pronto!

Sua infraestrutura está criada!

Próximos passos:
1. Conectar via SSH
2. Configurar wallet do banco
3. Deploy da aplicação

Consulte: [DEPLOY_GUIDE_WINDOWS.md](DEPLOY_GUIDE_WINDOWS.md) para os próximos passos.

---

## 🐛 Troubleshooting

### Erro: "No such file or directory"

Verifique se os caminhos estão corretos:
```powershell
# Verificar chave API
ls C:\Users\Mateus` Alves` Bassane\.oci\oci_api_key.pem

# Verificar chave SSH
ls C:\Users\Mateus` Alves` Bassane\.ssh\oci_key
```

### Erro: "Invalid fingerprint"

Regenere o fingerprint:
```bash
openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem 2>/dev/null | openssl md5 -c
```

### Erro: "Authentication failed"

Verifique:
1. Chave pública foi adicionada no console OCI
2. Fingerprint está correto
3. OCIDs estão corretos

---

**Boa sorte!** 🚀
