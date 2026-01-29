#!/bin/bash

# Script para regenerar a chave API OCI
# Execute este script no Git Bash

echo "=========================================="
echo "Regenerando Chave API OCI"
echo "=========================================="
echo ""

# Criar diretório se não existir
mkdir -p ~/.oci
chmod 700 ~/.oci

# Backup da chave antiga (se existir)
if [ -f ~/.oci/oci_api_key.pem ]; then
    echo "Fazendo backup da chave antiga..."
    mv ~/.oci/oci_api_key.pem ~/.oci/oci_api_key.pem.backup.$(date +%Y%m%d_%H%M%S)
fi

if [ -f ~/.oci/oci_api_key_public.pem ]; then
    mv ~/.oci/oci_api_key_public.pem ~/.oci/oci_api_key_public.pem.backup.$(date +%Y%m%d_%H%M%S)
fi

echo ""
echo "Gerando nova chave privada..."
openssl genrsa -out ~/.oci/oci_api_key.pem 2048
chmod 600 ~/.oci/oci_api_key.pem

echo ""
echo "Gerando chave pública..."
openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem

echo ""
echo "=========================================="
echo "CHAVE PÚBLICA (COPIE TUDO ABAIXO):"
echo "=========================================="
cat ~/.oci/oci_api_key_public.pem
echo ""
echo "=========================================="

echo ""
echo "Calculando fingerprint..."
FINGERPRINT=$(openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem 2>/dev/null | openssl md5 -c | cut -d'=' -f2 | tr -d ' ')

echo ""
echo "=========================================="
echo "FINGERPRINT:"
echo "=========================================="
echo "$FINGERPRINT"
echo ""

echo "=========================================="
echo "PRÓXIMOS PASSOS:"
echo "=========================================="
echo ""
echo "1. Copie a CHAVE PÚBLICA acima (incluindo BEGIN e END)"
echo ""
echo "2. Acesse o Console OCI:"
echo "   https://cloud.oracle.com"
echo ""
echo "3. Vá em: Ícone do usuário → User Settings → API Keys"
echo ""
echo "4. REMOVA a chave antiga (se houver)"
echo ""
echo "5. Clique em 'Add API Key'"
echo ""
echo "6. Selecione 'Paste Public Key'"
echo ""
echo "7. Cole a chave pública"
echo ""
echo "8. Clique em 'Add'"
echo ""
echo "9. Atualize o fingerprint no arquivo terraform.tfvars:"
echo "   fingerprint = \"$FINGERPRINT\""
echo ""
echo "=========================================="
echo "Chaves geradas com sucesso!"
echo "=========================================="
