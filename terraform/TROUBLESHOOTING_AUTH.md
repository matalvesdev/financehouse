# 🔧 Troubleshooting: Authentication Error (401-NotAuthenticated)

## ❌ Erro Atual

```
Error: 401-NotAuthenticated, Failed to verify the HTTP(S) Signature
```

Este erro significa que a autenticação com a API da Oracle Cloud não está funcionando. Vamos resolver!

## ✅ Solução: Verificar e Reconfigurar API Key

### Passo 1: Verificar se a Chave API Existe no Console OCI

1. Acesse: https://cloud.oracle.com
2. Faça login
3. Clique no ícone do usuário (canto superior direito)
4. Clique em **User Settings**
5. No menu lateral, clique em **API Keys**

**Você vê alguma chave listada?**

- ✅ **SIM**: Vá para o Passo 2
- ❌ **NÃO**: Vá para o Passo 3 (adicionar nova chave)

### Passo 2: Verificar Fingerprint

Se você já tem uma chave API no console, verifique se o fingerprint no arquivo `terraform.tfvars` corresponde ao fingerprint mostrado no console OCI.

**Fingerprint no arquivo**: `21:a7:89:38:99:84:fa:e1:f9:35:d5:4d:23:5c:91:5e`

**Fingerprint no console OCI**: ___________________________

**Eles são iguais?**

- ✅ **SIM**: Vá para o Passo 4 (verificar permissões)
- ❌ **NÃO**: Vá para o Passo 3 (adicionar nova chave)

### Passo 3: Adicionar Nova Chave API

#### 3.1 Gerar Nova Chave (Git Bash)

Abra o **Git Bash** e execute:

```bash
# Criar diretório (se não existir)
mkdir -p ~/.oci
chmod 700 ~/.oci

# Gerar nova chave privada
openssl genrsa -out ~/.oci/oci_api_key.pem 2048
chmod 600 ~/.oci/oci_api_key.pem

# Gerar chave pública
openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem

# Ver chave pública (COPIE TODO O CONTEÚDO)
cat ~/.oci/oci_api_key_public.pem
```

**Copie a chave pública** (incluindo as linhas BEGIN e END).

#### 3.2 Adicionar no Console OCI

1. No console OCI, vá em: **Identity & Security → Users → Seu usuário**
2. Clique em **API Keys** no menu lateral
3. Clique em **Add API Key**
4. Selecione **Paste Public Key**
5. Cole a chave pública que você copiou
6. Clique em **Add**

#### 3.3 Copiar Fingerprint

Após adicionar a chave, o console OCI mostrará o **fingerprint**. Copie-o!

Exemplo: `aa:bb:cc:dd:ee:ff:11:22:33:44:55:66:77:88:99:00`

#### 3.4 Obter Fingerprint via Git Bash (Alternativa)

No Git Bash:

```bash
openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem 2>/dev/null | openssl md5 -c | cut -d'=' -f2 | tr -d ' '
```

#### 3.5 Atualizar terraform.tfvars

Edite o arquivo: `terraform/environments/dev/terraform.tfvars`

Atualize a linha do fingerprint:

```hcl
fingerprint = "COLE_O_NOVO_FINGERPRINT_AQUI"
```

### Passo 4: Verificar Permissões do Usuário

O usuário precisa ter permissões para criar recursos na OCI.

#### 4.1 Verificar se o Usuário é Administrador

1. No console OCI, vá em: **Identity & Security → Users**
2. Clique no seu usuário
3. Clique em **Groups** no menu lateral

**Você está no grupo "Administrators"?**

- ✅ **SIM**: Ótimo! Vá para o Passo 5
- ❌ **NÃO**: Você precisa ser adicionado ao grupo Administrators ou ter políticas específicas

#### 4.2 Adicionar ao Grupo Administrators (se necessário)

1. No console OCI, vá em: **Identity & Security → Groups**
2. Clique em **Administrators**
3. Clique em **Add User to Group**
4. Selecione seu usuário
5. Clique em **Add**

### Passo 5: Verificar Compartment

Certifique-se de que o `compartment_ocid` no arquivo `terraform.tfvars` está correto.

**Opção 1: Usar Compartment Root (Recomendado)**

Use o mesmo OCID do tenancy:

```hcl
compartment_ocid = "ocid1.tenancy.oc1..aaaaaaaa3wdvrobn2k7rjh6wjyvv5xdnihdvqiylhsnq3acsjdeh4kpnutua"
```

**Opção 2: Criar Compartment Específico**

1. No console OCI, vá em: **Identity & Security → Compartments**
2. Clique em **Create Compartment**
3. Nome: `gestao-financeira-dev`
4. Clique em **Create Compartment**
5. Copie o OCID e atualize no `terraform.tfvars`

### Passo 6: Testar Novamente

```powershell
cd terraform
terraform plan -var-file="environments/dev/terraform.tfvars"
```

## 🔍 Checklist de Verificação

- [ ] Chave API adicionada no console OCI
- [ ] Fingerprint correto no `terraform.tfvars`
- [ ] Usuário no grupo "Administrators"
- [ ] Compartment OCID correto
- [ ] Arquivo `oci_api_key.pem` existe em `C:\Users\Mateus Alves Bassane\.oci\`
- [ ] Caminho da chave privada correto no `terraform.tfvars`

## 📝 Arquivo terraform.tfvars Correto

Seu arquivo deve estar assim:

```hcl
# OCI Authentication
tenancy_ocid     = "ocid1.tenancy.oc1..aaaaaaaa3wdvrobn2k7rjh6wjyvv5xdnihdvqiylhsnq3acsjdeh4kpnutua"
user_ocid        = "ocid1.user.oc1..aaaaaaaaakyso4bg54xnmrtmkunveqoq4wahtmjltn3v2pesnu6fhnim2eoq"
fingerprint      = "FINGERPRINT_CORRETO_AQUI"
private_key_path = "C:/Users/Mateus Alves Bassane/.oci/oci_api_key.pem"
region           = "sa-saopaulo-1"
compartment_ocid = "ocid1.tenancy.oc1..aaaaaaaa3wdvrobn2k7rjh6wjyvv5xdnihdvqiylhsnq3acsjdeh4kpnutua"
```

## 🐛 Outros Problemas Comuns

### Erro: "No such file or directory"

Verifique se o arquivo da chave existe:

```powershell
Test-Path "C:\Users\Mateus Alves Bassane\.oci\oci_api_key.pem"
```

Deve retornar `True`.

### Erro: "Invalid private key"

A chave privada pode estar corrompida. Gere uma nova seguindo o Passo 3.1.

### Erro: "Compartment not found"

Verifique se o compartment OCID está correto. Use o OCID do tenancy se não tiver certeza.

## 📞 Precisa de Ajuda?

Se o erro persistir após seguir todos os passos:

1. Verifique os logs detalhados: `terraform plan -var-file="environments/dev/terraform.tfvars" -debug`
2. Consulte a documentação oficial: https://docs.oracle.com/en-us/iaas/Content/API/Concepts/apisigningkey.htm
3. Verifique se sua conta OCI está ativa e verificada

---

**Boa sorte!** 🚀
