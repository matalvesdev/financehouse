.PHONY: help init plan apply destroy validate fmt clean output ssh wallet

# Variáveis
TERRAFORM := terraform
SSH_KEY := ~/.ssh/oci_key
ENV ?= dev
ENV_DIR := environments/$(ENV)

help: ## Mostra esta mensagem de ajuda
	@echo "Comandos disponíveis:"
	@echo ""
	@echo "Uso: make <comando> ENV=<dev|prod>"
	@echo "Exemplo: make apply ENV=prod"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "Ambiente atual: $(ENV)"

init: ## Inicializa o Terraform
	@echo "Inicializando Terraform para ambiente: $(ENV)..."
	@if [ ! -f "$(ENV_DIR)/terraform.tfvars" ]; then \
		echo "❌ Arquivo $(ENV_DIR)/terraform.tfvars não encontrado!"; \
		echo "Execute: make setup ENV=$(ENV)"; \
		exit 1; \
	fi
	$(TERRAFORM) init

validate: ## Valida a configuração
	@echo "Validando configuração..."
	$(TERRAFORM) validate

fmt: ## Formata os arquivos Terraform
	@echo "Formatando arquivos..."
	$(TERRAFORM) fmt -recursive

plan: validate ## Cria um plano de execução
	@echo "Criando plano de execução para $(ENV)..."
	$(TERRAFORM) plan -var-file="$(ENV_DIR)/terraform.tfvars"

apply: validate ## Aplica a configuração
	@echo "Aplicando configuração para $(ENV)..."
	$(TERRAFORM) apply -var-file="$(ENV_DIR)/terraform.tfvars"

destroy: ## Destrói toda a infraestrutura
	@echo "ATENÇÃO: Isso irá destruir TODOS os recursos do ambiente $(ENV)!"
	@read -p "Tem certeza? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		$(TERRAFORM) destroy -var-file="$(ENV_DIR)/terraform.tfvars"; \
	fi

output: ## Mostra os outputs
	@$(TERRAFORM) output

output-json: ## Mostra os outputs em JSON
	@$(TERRAFORM) output -json

ip: ## Mostra o IP público da instância
	@$(TERRAFORM) output -raw instance_public_ip

ssh: ## Conecta via SSH à instância
	@echo "Conectando à instância..."
	@IP=$$($(TERRAFORM) output -raw instance_public_ip); \
	ssh -i $(SSH_KEY) opc@$$IP

ssh-cmd: ## Mostra o comando SSH
	@$(TERRAFORM) output ssh_command

wallet: ## Baixa o wallet do banco de dados
	@echo "Wallet salvo em: $$($(TERRAFORM) output -raw wallet_location)"

deploy: ## Executa deploy na instância
	@echo "Executando deploy..."
	@IP=$$($(TERRAFORM) output -raw instance_public_ip); \
	ssh -i $(SSH_KEY) opc@$$IP "sudo /opt/gestao-financeira/deploy.sh"

logs: ## Mostra logs da aplicação
	@echo "Mostrando logs..."
	@IP=$$($(TERRAFORM) output -raw instance_public_ip); \
	ssh -i $(SSH_KEY) opc@$$IP "docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml logs -f"

status: ## Mostra status da aplicação
	@echo "Status da aplicação:"
	@IP=$$($(TERRAFORM) output -raw instance_public_ip); \
	ssh -i $(SSH_KEY) opc@$$IP "docker-compose -f /opt/gestao-financeira/app/docker-compose.prod.yml ps"

restart: ## Reinicia a aplicação
	@echo "Reiniciando aplicação..."
	@IP=$$($(TERRAFORM) output -raw instance_public_ip); \
	ssh -i $(SSH_KEY) opc@$$IP "sudo systemctl restart gestao-financeira"

clean: ## Remove arquivos temporários
	@echo "Limpando arquivos temporários..."
	@rm -rf .terraform
	@rm -f .terraform.lock.hcl
	@rm -f terraform.tfstate.backup
	@rm -f wallet.zip

setup: ## Setup inicial (cria terraform.tfvars)
	@echo "Setup para ambiente: $(ENV)"
	@if [ ! -f "$(ENV_DIR)/terraform.tfvars" ]; then \
		echo "Criando $(ENV_DIR)/terraform.tfvars..."; \
		cp "$(ENV_DIR)/terraform.tfvars.example" "$(ENV_DIR)/terraform.tfvars"; \
		echo "✅ Arquivo criado!"; \
		echo "⚠️  Edite $(ENV_DIR)/terraform.tfvars com suas credenciais"; \
	else \
		echo "❌ $(ENV_DIR)/terraform.tfvars já existe"; \
	fi

check-vars: ## Verifica se as variáveis estão configuradas
	@echo "Verificando variáveis para $(ENV)..."
	@if [ ! -f "$(ENV_DIR)/terraform.tfvars" ]; then \
		echo "❌ $(ENV_DIR)/terraform.tfvars não encontrado. Execute 'make setup ENV=$(ENV)'"; \
		exit 1; \
	fi
	@echo "✅ $(ENV_DIR)/terraform.tfvars encontrado"

full-deploy: check-vars init plan apply ## Deploy completo (init + plan + apply)
	@echo "✅ Deploy completo do ambiente $(ENV) concluído!"
	@echo ""
	@echo "Informações de acesso:"
	@$(MAKE) output ENV=$(ENV)

switch-env: ## Troca de ambiente (uso: make switch-env ENV=prod)
	@echo "Trocando para ambiente: $(ENV)"
	@if [ ! -d "$(ENV_DIR)" ]; then \
		echo "❌ Ambiente $(ENV) não existe!"; \
		echo "Ambientes disponíveis: dev, prod"; \
		exit 1; \
	fi
	@echo "✅ Ambiente configurado: $(ENV)"
	@echo "Use: make <comando> ENV=$(ENV)"

list-envs: ## Lista ambientes disponíveis
	@echo "Ambientes disponíveis:"
	@ls -d environments/*/ | sed 's|environments/||' | sed 's|/||'

compare-envs: ## Compara configurações entre ambientes
	@echo "Comparando dev vs prod..."
	@diff -u environments/dev/terraform.tfvars.example environments/prod/terraform.tfvars.example || true
	@echo "Atualizando aplicação..."
	@$(MAKE) deploy
	@$(MAKE) restart
	@echo "✅ Aplicação atualizada!"

backup-state: ## Faz backup do state file
	@echo "Fazendo backup do state..."
	@cp terraform.tfstate terraform.tfstate.backup.$$(date +%Y%m%d_%H%M%S)
	@echo "✅ Backup criado"

health: ## Verifica saúde da aplicação
	@echo "Verificando saúde da aplicação..."
	@IP=$$($(TERRAFORM) output -raw instance_public_ip); \
	curl -f http://$$IP:8080/actuator/health || echo "❌ Aplicação não está respondendo"

switch-env: ## Troca de ambiente (uso: make switch-env ENV=prod)
	@echo "Trocando para ambiente: $(ENV)"
	@if [ ! -d "$(ENV_DIR)" ]; then \
		echo "❌ Ambiente $(ENV) não existe!"; \
		echo "Ambientes disponíveis: dev, prod"; \
		exit 1; \
	fi
	@echo "✅ Ambiente configurado: $(ENV)"
	@echo "Use: make <comando> ENV=$(ENV)"

list-envs: ## Lista ambientes disponíveis
	@echo "Ambientes disponíveis:"
	@ls -d environments/*/ | sed 's|environments/||' | sed 's|/||'

compare-envs: ## Compara configurações entre ambientes
	@echo "Comparando dev vs prod..."
	@diff -u environments/dev/terraform.tfvars.example environments/prod/terraform.tfvars.example || true

promote-to-prod: ## Promove configuração de dev para prod (cuidado!)
	@echo "⚠️  ATENÇÃO: Isso irá copiar configurações de DEV para PROD!"
	@echo "Certifique-se de revisar as diferenças antes de aplicar."
	@read -p "Continuar? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		echo "Criando backup de prod..."; \
		cp environments/prod/terraform.tfvars environments/prod/terraform.tfvars.backup.$$(date +%Y%m%d_%H%M%S); \
		echo "Copiando configurações..."; \
		echo "⚠️  Revise manualmente: environments/prod/terraform.tfvars"; \
	fi
