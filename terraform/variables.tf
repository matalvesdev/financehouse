# OCI Authentication
variable "tenancy_ocid" {
  description = "OCID do tenancy da OCI"
  type        = string
}

variable "user_ocid" {
  description = "OCID do usuário da OCI"
  type        = string
}

variable "fingerprint" {
  description = "Fingerprint da chave API"
  type        = string
}

variable "private_key_path" {
  description = "Caminho para a chave privada da API"
  type        = string
}

variable "region" {
  description = "Região da OCI (ex: sa-saopaulo-1)"
  type        = string
  default     = "sa-saopaulo-1"
}

variable "compartment_ocid" {
  description = "OCID do compartment onde os recursos serão criados"
  type        = string
}

# Network Configuration
variable "vcn_cidr_block" {
  description = "CIDR block para a VCN"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block para a subnet pública"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR block para a subnet privada"
  type        = string
  default     = "10.0.2.0/24"
}

# Compute Configuration
variable "instance_shape" {
  description = "Shape da instância (Always Free: VM.Standard.E2.1.Micro ou VM.Standard.A1.Flex)"
  type        = string
  default     = "VM.Standard.E2.1.Micro"
}

variable "instance_ocpus" {
  description = "Número de OCPUs (para shapes flex, Always Free: até 4 OCPUs ARM)"
  type        = number
  default     = 1
}

variable "instance_memory_in_gbs" {
  description = "Memória em GB (para shapes flex, Always Free: até 24GB ARM)"
  type        = number
  default     = 6
}

variable "ssh_public_key" {
  description = "Chave SSH pública para acesso à instância"
  type        = string
}

# Database Configuration
variable "db_admin_password" {
  description = "Senha do usuário admin do banco de dados"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "gestaofinanceira"
}

variable "db_username" {
  description = "Nome do usuário do banco de dados"
  type        = string
  default     = "gestao_user"
}

variable "db_password" {
  description = "Senha do usuário do banco de dados"
  type        = string
  sensitive   = true
}

# Application Configuration
variable "app_name" {
  description = "Nome da aplicação"
  type        = string
  default     = "gestao-financeira"
}

variable "environment" {
  description = "Ambiente (dev, staging, production)"
  type        = string
  default     = "production"
}

variable "jwt_secret" {
  description = "Secret para geração de tokens JWT"
  type        = string
  sensitive   = true
}

variable "encryption_key" {
  description = "Chave para criptografia de dados sensíveis"
  type        = string
  sensitive   = true
}

# Tags
variable "tags" {
  description = "Tags para os recursos"
  type        = map(string)
  default = {
    Project     = "GestaoFinanceira"
    ManagedBy   = "Terraform"
    Environment = "Production"
  }
}
