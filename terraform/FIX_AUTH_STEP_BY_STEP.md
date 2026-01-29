# 🔧 Corrigir Erro de Autenticação - Passo a Passo

## Problema

Erro: `401-NotAuthenticated, Failed to verify the HTTP(S) Signature`

## Causa

A assinatura HTTP não está sendo verificada corretamente. Isso pode acontecer por:
- Formato incorreto da chave privada
- Quebras de linha incorretas no arquivo
- Chave não sincronizada com o console OCI

## ✅ Solução: Regenerar Chave API

### Passo 1: Abrir Git Bash

1. Pressione `Windows + R`
2. Digite: `git-bash`
3. Pressione Enter

### Passo 2: Executar Script de Regeneração

No Git Bash, execute:

```bash
cd /c/Users/Mateus\ Alves\ Bassane/Desktop/financehouse/terraform
bash regenerate-api-key.sh
```

O script irá:
- Fazer backup da chave antiga
- Gerar nova chave privada
- Gerar nova chave pública
- Calcular o novo fingerprint
- Mostrar tudo na tela

### Passo 3: Copiar Chave Pública

O script mostrará algo assim:

```
==========================================
CHAVE PÚBLICA (COPIE TUDO ABAIXO):
==========================================
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
...
-----END PUBLIC KEY-----
==========================================
```

**COPIE TUDO** (incluindo as linhas BEGIN e END)

### Passo 4: Copiar Fingerprint

O script também mostrará:

```
==========================================
FINGERPRINT:
==========================================
aa:bb:cc:dd:ee:ff:11:22:33:44:55:66:77:88:99:00
```

**COPIE o fingerprint**

### Passo 5: Atualizar Console OCI

1. Acesse: https://cloud.oracle.com
2. Faça login
3. Clique no ícone do usuário (canto superior direito)
4. Clique em **User Settings**
5. No menu lateral, clique em **API Keys**

#### 5.1 Remover Chave Antiga

6. Encontre a chave com fingerprint: `21:a7:89:38:99:84:fa:e1:f9:35:d5:4d:23:5c:91:5e`
7. Clique nos três pontos (⋮) ao lado da chave
8. Clique em **Delete**
9. Confirme a exclusão

#### 5.2 Adicionar Nova Chave

10. Clique em **Add API Key**
11. Selecione **Paste Public Key**
12. Cole a chave pública que você copiou no Passo 3
13. Clique em **Add**

### Passo 6: Atualizar terraform.tfvars

Edite o arquivo: `terraform/environments/dev/terraform.tfvars`

Atualize a linha do fingerprint com o novo valor do Passo 4:

```hcl
fingerprint = "COLE_O_NOVO_FINGERPRINT_AQUI"
```

### Passo 7: Testar

No PowerShell:

```powershell
cd terraform
terraform plan -var-file="environments/dev/terraform.tfvars"
```

## ✅ Deve Funcionar!

Se ainda houver erro, verifique:

1. **Chave pública foi adicionada no console OCI?**
   - Vá em: User Settings → API Keys
   - Você deve ver a nova chave listada

2. **Fingerprint está correto no terraform.tfvars?**
   - Compare com o fingerprint mostrado no console OCI

3. **Caminho da chave privada está correto?**
   - Deve ser: `C:/Users/Mateus Alves Bassane/.oci/oci_api_key.pem`

4. **Arquivo da chave privada existe?**
   ```powershell
   Test-Path "C:\Users\Mateus Alves Bassane\.oci\oci_api_key.pem"
   ```
   Deve retornar `True`

## 🆘 Se Ainda Não Funcionar

Tente criar a chave manualmente:

### Opção Alternativa: Criar Chave Manualmente

No Git Bash:

```bash
# Criar diretório
mkdir -p ~/.oci
chmod 700 ~/.oci

# Gerar chave privada
openssl genrsa -out ~/.oci/oci_api_key.pem 2048
chmod 600 ~/.oci/oci_api_key.pem

# Gerar chave pública
openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem

# Ver chave pública
cat ~/.oci/oci_api_key_public.pem

# Ver fingerprint
openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem 2>/dev/null | openssl md5 -c
```

Depois siga os passos 3 a 7 acima.

## 📞 Suporte

Se o problema persistir, pode ser:

1. **Problema de permissões no Windows**
   - Tente executar o PowerShell como Administrador

2. **Problema com o formato do arquivo**
   - Certifique-se de que o arquivo não foi editado em um editor de texto do Windows

3. **Problema com a conta OCI**
   - Verifique se sua conta está ativa e verificada
   - Verifique se você está no grupo Administrators

---

**Boa sorte!** 🚀
