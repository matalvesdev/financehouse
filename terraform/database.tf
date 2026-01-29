# Autonomous Database (Always Free Tier)
resource "oci_database_autonomous_database" "main" {
  compartment_id           = var.compartment_ocid
  db_name                  = replace(var.db_name, "-", "")
  display_name             = "${var.app_name}-adb"
  admin_password           = var.db_admin_password
  db_version               = "19c"
  db_workload              = "OLTP"
  is_auto_scaling_enabled  = false
  is_free_tier             = true
  license_model            = "LICENSE_INCLUDED"
  
  # Always Free: 1 OCPU, 20GB storage
  cpu_core_count           = 1
  data_storage_size_in_tbs = 1
  
  # Network configuration
  subnet_id                = oci_core_subnet.private.id
  nsg_ids                  = [oci_core_network_security_group.database.id]
  
  # Backup configuration
  is_auto_scaling_for_storage_enabled = false
  
  freeform_tags = var.tags
}

# Network Security Group para o banco de dados
resource "oci_core_network_security_group" "database" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.app_name}-db-nsg"
  
  freeform_tags = var.tags
}

# Regra NSG: Permitir tráfego PostgreSQL da subnet pública
resource "oci_core_network_security_group_security_rule" "db_ingress_from_app" {
  network_security_group_id = oci_core_network_security_group.database.id
  direction                 = "INGRESS"
  protocol                  = "6" # TCP
  source                    = var.public_subnet_cidr
  source_type               = "CIDR_BLOCK"
  
  tcp_options {
    destination_port_range {
      min = 1521
      max = 1522
    }
  }
  
  description = "Allow Oracle DB access from application subnet"
}

# Regra NSG: Permitir tráfego de saída
resource "oci_core_network_security_group_security_rule" "db_egress" {
  network_security_group_id = oci_core_network_security_group.database.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
  
  description = "Allow all outbound traffic"
}

# Wallet para conexão segura (será baixado localmente)
resource "oci_database_autonomous_database_wallet" "main" {
  autonomous_database_id = oci_database_autonomous_database.main.id
  password               = var.db_admin_password
  base64_encode_content  = true
}

# Salvar wallet localmente
resource "local_file" "wallet" {
  content_base64 = oci_database_autonomous_database_wallet.main.content
  filename       = "${path.module}/wallet.zip"
  
  depends_on = [oci_database_autonomous_database_wallet.main]
}
