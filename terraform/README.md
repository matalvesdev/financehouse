# Terraform - Oracle Cloud Infrastructure (OCI)

Este diretório contém a configuração Terraform para deploy da aplicação de Gestão Financeira Doméstica na Oracle Cloud Infrastructure usando recursos **Always Free**.

## 🌟 Novidade: Múltiplos Ambientes

A infraestrutura agora suporta ambientes separados:
- **Development (dev)**: Para testes e desenvolvimento
- **Production (prod)**: Para aplicação em produção

Cada ambiente tem recursos OCI isolados, configurações independentes e credenciais separadas.

**Guia completo**: [MULTI_ENV_GUIDE.md](MULTI_ENV_GUIDE.md)

## Recursos Always Free Utilizados

A configuração utiliza os seguintes recursos Always Free da OCI:

### Compute
- **2x VM.Standard.E2.1.Micro** (AMD x86)
  - 1 OCPU, 1GB RAM cada
  - 0.48 GB/s network bandwidth
  - OU
- **VM.Standard.A1.Flex** (ARM Ampere A1)
  - Até 4 OCPUs e 24GB RAM (compartilhado entre instâncias)

### Database
- **Autonomous Database** (Always Free)
  - 1 OCPU
  - 20 GB de armazenamento
  - Backups automáticos

### Network
- **VCN** (Virtual Cloud Network)
- **2 Subnets** (pública e privada)
- **Internet Gateway**
- **NAT Gateway**
- **Service Gateway**
- **Security Lists e Network Security Groups**

### Storage
- **2x Block Volumes** de 200GB (total)
- **10GB Object Storage**
- **10GB Archive Storage**

## Pré-requisitos

1. **Conta OCI**: Crie uma conta gratuita em [oracle.com/cloud/free](https://www.oracle.com/cloud/free/)

2. **Terraform**: Instale o Terraform >= 1.0
   ```bash
   # Windows (Chocolatey)
   choco install terraform
   
   # macOS (Homebrew)
   brew install terraform
   
   # Linux
   wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
   unzip terraform_1.6.0_linux_amd64.zip
   sudo mv terraform /usr/local/bin/
   ```

3. **OCI CLI** (opcional, mas recomendado):
   ```bash
   # Instalar OCI CLI
   bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
   
   # Configurar
   oci setup config
   ```

4. **Chave SSH**: Gere um par de chaves SSH se ainda não tiver
   ```bash
   ssh-keygen -t rsa -b 4096 -f ~/.ssh/oci_key
   ```

## Setup Inicial

### Opção 1: Multi-Ambiente (Recomendado)

Configure ambos os ambientes (dev e prod) de uma vez:

```bash
cd terraform
chmod +x setup-multi-env.sh
./setup-multi-env.sh
```

### Opção 2: Ambiente Único

Configure apenas um ambiente:

```bash
cd terraform
chmod +x setup.sh
./setup.sh
```

### 2. Configurar Variáveis

No console da OCI, obtenha as seguintes informações:

1. **Tenancy OCID**: 
   - Menu → Administration → Tenancy Details
   - Copie o OCID

2. **User OCID**:
   - Menu → Identity → Users → Seu usuário
   - Copie o OCID

3. **Compartment OCID**:
   - Menu → Identity → Compartments
   - Selecione ou crie um compartment
   - Copie o OCID

4. **API Key**:
   ```bash
   # Gerar chave API
   mkdir -p ~/.oci
   openssl genrsa -out ~/.oci/oci_api_key.pem 2048
   chmod 600 ~/.oci/oci_api_key.pem
   openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem
   
   # Obter fingerprint
   openssl rsa -pubout -outform DER -in ~/.oci/oci_api_key.pem | openssl md5 -c
   ```

5. **Adicionar chave pública no console OCI**:
   - Menu → Identity → Users → Seu usuário → API Keys
   - Add API Key → Paste Public Key
   - Cole o conteúdo de `~/.oci/oci_api_key_public.pem`

### 2. Configurar Variáveis

```bash
# Copiar arquivo de exemplo
cp terraform.tfvars.example terraform.tfvars

# Editar com suas credenciais
nano terraform.tfvars
```

Preencha todas as variáveis obrigatórias:
- OCIDs (tenancy, user, compartment)
- Fingerprint da API key
- Caminho da chave privada
- Região (ex: sa-saopaulo-1)
- Chave SSH pública
- Senhas do banco de dados
- JWT secret e encryption key

### 3. Gerar Secrets

```bash
# Gerar JWT secret (32 caracteres)
openssl rand -base64 32

# Gerar encryption key (32 caracteres)
openssl rand -base64 32
```

## Deploy

### 1. Inicializar Terraform

```bash
cd terraform
terraform init
```

### 2. Validar Configuração

```bash
terraform validate
terraform fmt
```

### 3. Planejar Deploy

```bash
terraform plan
```

Revise cuidadosamente o plano para garantir que apenas recursos Always Free serão criados.

### 4. Aplicar Configuração

```bash
terraform apply
```

Digite `yes` quando solicitado. O processo levará cerca de 10-15 minutos.

### 5. Obter Informações de Deploy

```bash
# Ver todos os outputs
terraform output

# Ver IP público da instância
terraform output instance_public_ip

# Ver comando SSH
terraform output ssh_command

# Ver informações do banco de dados
terraform output database_connection_string
```

## Pós-Deploy

### 1. Conectar à Instância

```bash
# Usar o comando SSH do output
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
```

### 2. Verificar Instalação

```bash
# Verificar Docker
docker --version
docker-compose --version

# Verificar diretório da aplicação
ls -la /opt/gestao-financeira/

# Ver logs de inicialização
sudo cat /var/log/cloud-init-output.log
```

### 3. Deploy da Aplicação

```bash
# Editar script de deploy com URL do seu repositório
sudo nano /opt/gestao-financeira/deploy.sh

# Executar deploy
sudo /opt/gestao-financeira/deploy.sh
```

### 4. Configurar Wallet do Banco de Dados

O wallet foi baixado automaticamente para `terraform/wallet.zip`. Você precisa copiá-lo para a instância:

```bash
# Copiar wallet para a instância
scp -i ~/.ssh/oci_key wallet.zip opc@<IP_PUBLICO>:/opt/gestao-financeira/wallet/

# Na instância, extrair wallet
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
cd /opt/gestao-financeira/wallet
unzip wallet.zip
chmod 600 *
```

### 5. Verificar Aplicação

```bash
# Ver logs da aplicação
docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml logs -f

# Verificar status
docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml ps

# Testar endpoint
curl http://localhost:8080/actuator/health
```

### 6. Configurar DNS (Opcional)

Se você tem um domínio, aponte-o para o IP público da instância:

```
A record: @ -> <IP_PUBLICO>
A record: www -> <IP_PUBLICO>
```

## Gerenciamento

### Atualizar Aplicação

```bash
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
sudo /opt/gestao-financeira/deploy.sh
```

### Ver Logs

```bash
# Logs da aplicação
docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml logs -f

# Logs do sistema
sudo journalctl -u gestao-financeira.service -f
```

### Reiniciar Aplicação

```bash
# Via systemd
sudo systemctl restart gestao-financeira

# Via Docker Compose
cd /opt/gestao-financeira/app
docker-compose -f docker-compose.prod.yml restart
```

### Backup do Banco de Dados

O Autonomous Database faz backups automáticos. Para backup manual:

```bash
# No console OCI
# Database → Autonomous Database → Seu DB → More Actions → Create Manual Backup
```

## Monitoramento

### Métricas da Instância

No console OCI:
- Compute → Instances → Sua instância → Metrics

### Métricas do Banco de Dados

No console OCI:
- Database → Autonomous Database → Seu DB → Performance Hub

### Logs

```bash
# Logs da aplicação
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
tail -f /opt/gestao-financeira/logs/*.log
```

## Custos

Esta configuração usa **apenas recursos Always Free**, portanto:
- ✅ **Custo: R$ 0,00/mês**
- ✅ Sem limite de tempo
- ✅ Sem necessidade de cartão de crédito após período trial

**Importante**: Certifique-se de não exceder os limites Always Free:
- Máximo 2 instâncias VM.Standard.E2.1.Micro OU 4 OCPUs ARM
- Máximo 1 Autonomous Database Always Free
- Máximo 200GB Block Storage

## Destruir Infraestrutura

Para remover todos os recursos:

```bash
terraform destroy
```

**Atenção**: Isso removerá TODOS os recursos, incluindo o banco de dados. Faça backup antes!

## Troubleshooting

### Erro: "Service limit exceeded"

Você pode ter atingido o limite de recursos Always Free. Verifique:
```bash
oci limits resource-availability get --compartment-id <COMPARTMENT_OCID> --service-name compute
```

### Erro: "Out of host capacity"

A região pode estar sem capacidade. Tente:
1. Mudar para outra região
2. Usar VM.Standard.A1.Flex (ARM) em vez de E2.1.Micro
3. Tentar novamente mais tarde

### Instância não responde

```bash
# Verificar console serial
# No console OCI: Compute → Instances → Sua instância → Console Connection

# Reiniciar instância
terraform taint oci_core_instance.app
terraform apply
```

### Problemas de conexão com banco de dados

```bash
# Verificar security lists e NSGs
terraform state show oci_core_network_security_group.database

# Testar conectividade
ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
telnet <DB_HOST> 1521
```

## Segurança

### Recomendações

1. **Nunca commite** `terraform.tfvars` ou arquivos com credenciais
2. **Use secrets management** para produção (OCI Vault)
3. **Habilite MFA** na conta OCI
4. **Rotacione senhas** regularmente
5. **Monitore logs** de acesso
6. **Mantenha sistema atualizado**:
   ```bash
   ssh -i ~/.ssh/oci_key opc@<IP_PUBLICO>
   sudo yum update -y
   ```

### Hardening

```bash
# Desabilitar login root via SSH
sudo sed -i 's/PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config
sudo systemctl restart sshd

# Configurar fail2ban
sudo yum install -y fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban

# Configurar firewall
sudo firewall-cmd --permanent --remove-service=ssh
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="SEU_IP" port port="22" protocol="tcp" accept'
sudo firewall-cmd --reload
```

## Suporte

- **Documentação OCI**: https://docs.oracle.com/en-us/iaas/
- **Terraform OCI Provider**: https://registry.terraform.io/providers/oracle/oci/
- **OCI Always Free**: https://www.oracle.com/cloud/free/

## Licença

Este projeto está sob a licença MIT.
