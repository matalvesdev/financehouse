output "instance_public_ip" {
  description = "IP público da instância de aplicação"
  value       = oci_core_instance.app.public_ip
}

output "instance_private_ip" {
  description = "IP privado da instância de aplicação"
  value       = oci_core_instance.app.private_ip
}

output "instance_id" {
  description = "OCID da instância"
  value       = oci_core_instance.app.id
}

output "database_connection_string" {
  description = "String de conexão do Autonomous Database"
  value       = oci_database_autonomous_database.main.connection_strings[0].profiles[0].value
  sensitive   = true
}

output "database_id" {
  description = "OCID do Autonomous Database"
  value       = oci_database_autonomous_database.main.id
}

output "database_name" {
  description = "Nome do banco de dados"
  value       = oci_database_autonomous_database.main.db_name
}

output "vcn_id" {
  description = "OCID da VCN"
  value       = oci_core_vcn.main.id
}

output "public_subnet_id" {
  description = "OCID da subnet pública"
  value       = oci_core_subnet.public.id
}

output "private_subnet_id" {
  description = "OCID da subnet privada"
  value       = oci_core_subnet.private.id
}

output "ssh_command" {
  description = "Comando SSH para conectar à instância"
  value       = "ssh -i <sua-chave-privada> opc@${oci_core_instance.app.public_ip}"
}

output "application_url" {
  description = "URL da aplicação"
  value       = "http://${oci_core_instance.app.public_ip}"
}

output "wallet_location" {
  description = "Localização do wallet do banco de dados"
  value       = "${path.module}/wallet.zip"
}

output "deployment_info" {
  description = "Informações de deployment"
  value = {
    instance_ip      = oci_core_instance.app.public_ip
    database_name    = oci_database_autonomous_database.main.db_name
    app_directory    = "/opt/${var.app_name}"
    deploy_script    = "/opt/${var.app_name}/deploy.sh"
    service_name     = "${var.app_name}.service"
  }
}
