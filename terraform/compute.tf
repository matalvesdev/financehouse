# Obter imagem mais recente do Oracle Linux 8
data "oci_core_images" "oracle_linux" {
  compartment_id           = var.compartment_ocid
  operating_system         = "Oracle Linux"
  operating_system_version = "8"
  shape                    = var.instance_shape
  sort_by                  = "TIMECREATED"
  sort_order               = "DESC"
}

# Instância de Computação (Always Free Eligible)
resource "oci_core_instance" "app" {
  compartment_id      = var.compartment_ocid
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
  display_name        = "${var.app_name}-app-instance"
  shape               = var.instance_shape
  
  # Para shapes flex (VM.Standard.A1.Flex - ARM Always Free)
  dynamic "shape_config" {
    for_each = length(regexall("Flex", var.instance_shape)) > 0 ? [1] : []
    content {
      ocpus         = var.instance_ocpus
      memory_in_gbs = var.instance_memory_in_gbs
    }
  }
  
  create_vnic_details {
    subnet_id        = oci_core_subnet.public.id
    display_name     = "${var.app_name}-vnic"
    assign_public_ip = true
    hostname_label   = replace(var.app_name, "-", "")
  }
  
  source_details {
    source_type = "image"
    source_id   = data.oci_core_images.oracle_linux.images[0].id
  }
  
  metadata = {
    ssh_authorized_keys = var.ssh_public_key
    user_data = base64encode(templatefile("${path.module}/cloud-init.yaml", {
      db_host           = oci_database_autonomous_database.main.connection_strings[0].profiles[0].value
      db_name           = var.db_name
      db_username       = var.db_username
      db_password       = var.db_password
      jwt_secret        = var.jwt_secret
      encryption_key    = var.encryption_key
      app_name          = var.app_name
    }))
  }
  
  freeform_tags = var.tags
  
  preserve_boot_volume = false
}

# Availability Domains
data "oci_identity_availability_domains" "ads" {
  compartment_id = var.compartment_ocid
}

# Boot Volume Backup Policy (opcional, mas recomendado)
resource "oci_core_volume_backup_policy_assignment" "app_boot_volume" {
  asset_id  = oci_core_instance.app.boot_volume_id
  policy_id = data.oci_core_volume_backup_policies.default_policies.volume_backup_policies[0].id
}

data "oci_core_volume_backup_policies" "default_policies" {
  filter {
    name   = "display_name"
    values = ["bronze"]
  }
}
