# ✅ Checklist Rápido de Deploy

Use este checklist para não esquecer nenhum passo!

## 📋 Antes de Começar

- [ ] Conta OCI criada (https://www.oracle.com/cloud/free/)
- [ ] Email verificado
- [ ] Login no console OCI funcionando

## 🔑 Obter Credenciais (5 minutos)

### No Console OCI:

- [ ] **Tenancy OCID**: Menu → Administration → Tenancy Details → Copiar OCID
- [ ] **User OCID**: Menu → Identity & Security → Users → Seu usuário → Copiar OCID
- [ ] **Compartment OCID**: Menu → Identity & Security → Compartments → Copiar OCID
- [ ] **Região**: Anotar (ex: sa-saopaulo-1)

## 🔐 Gerar Chaves (5 minutos)

### Chave API OCI:

```powershell
# Criar diretório
mkdir $HOME\.oci

# Gerar chave (use Git Bash)
openssl genrsa -out $HOME\.oci\oci_api_key.pem 2048
openssl rsa -pubout -in $HOME\.oci\oci_api_key.pem -out $HOME\.oci\oci_api_key_public.pem

# Ver chave pública
cat $HOME\.oci\oci_api_key_public.pem
```

- [ ] Chave privada gerada
- [ ] Chave pública gerada
- [ ] Chave pública copiada

### Adicionar no Console OCI:

- [ ] Identity & Security → Users → Seu usuário → API Keys
- [ ] Add API Key → Paste Public Key
- [ ] Colar chave pública
- [ ] Clicar em Add

### Obter Fingerprint:

```powershell
openssl rsa -pubout -outform DER -in $HOME\.oci\oci_api_key.pem | openssl md5 -c
```

- [ ] Fingerprint copiado

### Chave SSH:

```powershell
mkdir $HOME\.ssh
ssh-keygen -t rsa -b 4096 -f $HOME\.ssh\oci_key -N ""
cat $HOME\.ssh\oci_key.pub
```

- [ ] Chave SSH gerada
- [ ] Chave pública SSH copiada

## 📝 Criar Arquivo de Configuração (5 minutos)

Criar arquivo: `terraform\environments\dev\terraform.tfvars`

```hcl
tenancy_ocid     = "COLE_AQUI"
user_ocid        = "COLE_AQUI"
fingerprint      = "COLE_AQUI"
private_key_path = "C:/Users/Mateus Alves Bassane/.oci/oci_api_key.pem"
region           = "sa-saopaulo-1"
compartment_ocid = "COLE_AQUI"

environment = "development"

vcn_cidr_block       = "10.0.0.0/16"
public_subnet_cidr   = "10.0.1.0/24"
private_subnet_cidr  = "10.0.2.0/24"

instance_shape = "VM.Standard.E2.1.Micro"

ssh_public_key = "COLE_CHAVE_SSH_PUBLICA_AQUI"

db_admin_password = "DevPassword123!@#"
db_name           = "gestaofinanceiradev"
db_username       = "gestao_dev"
db_password       = "DevUserPass123!@#"

app_name       = "gestao-financeira-dev"
jwt_secret     = "dev-jwt-secret-32-chars-change-this"
encryption_key = "dev-encryption-key-32-chars-here"

tags = {
  Project     = "GestaoFinanceira"
  ManagedBy   = "Terraform"
  Environment = "Development"
}
```

- [ ] Arquivo criado
- [ ] Tenancy OCID preenchido
- [ ] User OCID preenchido
- [ ] Fingerprint preenchido
- [ ] Compartment OCID preenchido
- [ ] Chave SSH pública preenchida

## 🚀 Deploy (15 minutos)

```powershell
cd terraform

# Inicializar
terraform init

# Validar
terraform validate

# Ver plano
terraform plan -var-file="environments/dev/terraform.tfvars"

# Aplicar
terraform apply -var-file="environments/dev/terraform.tfvars"
```

- [ ] `terraform init` executado
- [ ] `terraform validate` passou
- [ ] `terraform plan` revisado
- [ ] `terraform apply` executado
- [ ] Digitado `yes` para confirmar
- [ ] Aguardado ~10-15 minutos
- [ ] Deploy concluído sem erros

## 📊 Ver Resultados

```powershell
terraform output
```

- [ ] IP público anotado: ___________________________
- [ ] Connection string anotada
- [ ] Wallet baixado (wallet.zip)

## 🔌 Conectar à Instância

```powershell
ssh -i $HOME\.ssh\oci_key opc@<IP_PUBLICO>
```

- [ ] Conexão SSH funcionando
- [ ] Docker instalado
- [ ] Diretório `/opt/gestao-financeira-dev/` existe

## 💾 Configurar Wallet

```powershell
# Copiar wallet
scp -i $HOME\.ssh\oci_key wallet.zip opc@<IP_PUBLICO>:/tmp/

# Na instância:
sudo mkdir -p /opt/gestao-financeira-dev/wallet
sudo mv /tmp/wallet.zip /opt/gestao-financeira-dev/wallet/
cd /opt/gestao-financeira-dev/wallet
sudo unzip wallet.zip
sudo chmod 600 *
```

- [ ] Wallet copiado
- [ ] Wallet extraído
- [ ] Permissões configuradas

## 📦 Deploy da Aplicação

```bash
# Editar script
sudo nano /opt/gestao-financeira-dev/deploy.sh
# Alterar: REPO_URL="https://github.com/matalvesdev/financehouse.git"

# Executar
sudo /opt/gestao-financeira-dev/deploy.sh
```

- [ ] Script editado com URL do repositório
- [ ] Deploy executado
- [ ] Containers rodando

## ✅ Verificar

```bash
# Status
docker-compose -f /opt/gestao-financeira-dev/app/docker-compose.prod.yml ps

# Logs
docker-compose -f /opt/gestao-financeira-dev/app/docker-compose.prod.yml logs -f

# Health check
curl http://localhost:8080/actuator/health
```

- [ ] Containers rodando
- [ ] Logs sem erros críticos
- [ ] Health check respondendo

## 🌐 Acessar Aplicação

Abrir navegador: `http://<IP_PUBLICO>`

- [ ] Frontend carregando
- [ ] Página de login aparecendo
- [ ] Consegue criar conta
- [ ] Consegue fazer login

## 🎉 Deploy Completo!

**Data**: ___________________________

**IP Público**: ___________________________

**Ambiente**: Development

**Custo**: R$ 0,00/mês (Always Free)

---

## 📝 Próximos Passos

- [ ] Configurar domínio (opcional)
- [ ] Configurar HTTPS (recomendado)
- [ ] Deploy em produção
- [ ] Configurar CI/CD
- [ ] Configurar monitoramento

## 🐛 Se algo der errado:

1. Verificar logs: `sudo cat /var/log/cloud-init-output.log`
2. Verificar security lists no console OCI
3. Verificar se chave API está ativa
4. Consultar: [DEPLOY_GUIDE_WINDOWS.md](DEPLOY_GUIDE_WINDOWS.md)

---

**Boa sorte!** 🚀
