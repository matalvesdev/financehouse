# Guia Rápido - Deploy OCI

Este guia mostra como fazer deploy da aplicação na Oracle Cloud Infrastructure em poucos minutos.

## Pré-requisitos

1. Conta OCI (gratuita): https://www.oracle.com/cloud/free/
2. Terraform instalado
3. Git Bash (Windows) ou terminal Unix

## Setup Rápido (5 minutos)

### 1. Execute o script de setup

```bash
cd terraform
chmod +x setup.sh
./setup.sh
```

O script irá:
- ✅ Gerar chaves API e SSH automaticamente
- ✅ Solicitar suas credenciais OCI
- ✅ Gerar secrets da aplicação
- ✅ Criar arquivo `terraform.tfvars` configurado

### 2. Adicione a chave API no console OCI

Durante o setup, você verá uma chave pública. Adicione-a no console:

1. Acesse: https://cloud.oracle.com
2. Menu → Identity & Security → Users → Seu usuário
3. API Keys → Add API Key
4. Paste Public Key → Cole a chave mostrada no terminal

### 3. Deploy!

```bash
# Opção 1: Usando Makefile (recomendado)
make full-deploy

# Opção 2: Comandos Terraform diretos
terraform init
terraform plan
terraform apply
```

Aguarde ~10-15 minutos. ☕

### 4. Acesse sua aplicação

```bash
# Ver IP público
make ip

# Conectar via SSH
make ssh

# Ver todas as informações
make output
```

Pronto! Sua aplicação está rodando em: `http://<IP_PUBLICO>`

## Comandos Úteis

```bash
# Ver ajuda
make help

# Deploy da aplicação
make deploy

# Ver logs
make logs

# Reiniciar
make restart

# Verificar saúde
make health

# Destruir tudo
make destroy
```

## Obtendo Credenciais OCI

### Tenancy OCID
1. Console OCI → Menu (☰)
2. Administration → Tenancy Details
3. Copie o OCID

### User OCID
1. Console OCI → Menu (☰)
2. Identity & Security → Users
3. Clique no seu usuário
4. Copie o OCID

### Compartment OCID
1. Console OCI → Menu (☰)
2. Identity & Security → Compartments
3. Selecione ou crie um compartment
4. Copie o OCID

### Região
Exemplos:
- São Paulo: `sa-saopaulo-1`
- Vinhedo: `sa-vinhedo-1`
- Santiago: `sa-santiago-1`
- US East: `us-ashburn-1`

Lista completa: https://docs.oracle.com/en-us/iaas/Content/General/Concepts/regions.htm

## Recursos Criados (Always Free)

- ✅ 1x Compute Instance (VM.Standard.E2.1.Micro)
- ✅ 1x Autonomous Database (20GB)
- ✅ 1x VCN com subnets públicas e privadas
- ✅ Internet Gateway, NAT Gateway, Service Gateway
- ✅ Security Lists e Network Security Groups

**Custo: R$ 0,00/mês** 🎉

## Troubleshooting

### "Service limit exceeded"
Você pode ter atingido o limite Always Free. Verifique no console:
- Governance → Limits, Quotas and Usage

### "Out of host capacity"
A região está sem capacidade. Tente:
1. Outra região
2. VM.Standard.A1.Flex (ARM) em vez de E2.1.Micro
3. Aguarde algumas horas e tente novamente

### Instância não responde
```bash
# Verificar console serial no OCI
# Compute → Instances → Sua instância → Console Connection

# Ou recriar instância
terraform taint oci_core_instance.app
terraform apply
```

### Erro de autenticação
Verifique se:
1. Chave API foi adicionada no console OCI
2. Fingerprint está correto
3. Caminho da chave privada está correto

## Próximos Passos

1. **Configurar domínio**: Aponte seu DNS para o IP público
2. **Configurar HTTPS**: Use Let's Encrypt com Certbot
3. **Configurar CI/CD**: Automatize deploys com GitHub Actions
4. **Monitoramento**: Configure alertas no OCI

## Suporte

- 📖 Documentação completa: [README.md](README.md)
- 🌐 Docs OCI: https://docs.oracle.com/en-us/iaas/
- 💬 Terraform OCI: https://registry.terraform.io/providers/oracle/oci/

## Segurança

⚠️ **IMPORTANTE**:
- Nunca commite `terraform.tfvars`
- Mantenha suas chaves privadas seguras
- Use senhas fortes (mínimo 12 caracteres)
- Habilite MFA na conta OCI
- Rotacione senhas regularmente

---

Feito com ❤️ para a comunidade
