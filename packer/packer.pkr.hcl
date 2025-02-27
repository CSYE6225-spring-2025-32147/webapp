packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
    googlecompute = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}


variable "DB_URL" { type = string }
variable "DB_USRNAME" { type = string }
variable "DB_PASSWORD" { type = string }
variable "DB_NAME" { type = string }
variable "SSH_USERNAME" { type = string }
variable "SUBNET_ID" { type = string }
variable "INSTANCE_TYPE" { type = string }
variable "REGION" {
  type    = string
  default = "us-east-1"
}
variable "AMI_NAME" {
  type    = string
  default = "csye6225_health_checker"
}
variable "AMI_DESCRIPTION" {
  type    = string
  default = "AMI for CSYE6225 Assignment 4"
}
variable "VOLUME_SIZE" {
  type    = number
  default = 8
}
variable "VOLUME_TYPE" {
  type    = string
  default = "gp3"
}
variable "OWNER_ID" {
  type    = string
  default = "099720109477"
}

variable "AMI_USER" {
  type = string
}

variable "GCP_PROJECT_ID" {
  type    = string
  default = "gcp-dev-452117"
}
variable "GCP_ZONE" {
  type    = string
  default = "us-central1-a"
}
variable "GCP_MACHINE_TYPE" {
  type    = string
  default = "e2-mirco"
}
variable "GCP_IMAGE_NAME" {
  type    = string
  default = "csye6225-health-checker-gcp"
}
variable "GCP_IMAGE_DESCRIPTION" {
  type    = string
  default = "Custom GCP Image for CSYE6225"
}
variable "GCP_DISK_SIZE" {
  type    = number
  default = 25
}
variable "GCP_DISK_TYPE" {
  type    = string
  default = "pd-balanced"
}

# Amazon AMI Source Configuration
source "amazon-ebs" "my-ami" {
  ami_name        = "${var.AMI_NAME}_${formatdate("YYYY_MM_DD_HH_mm_ss", timestamp())}"
  ami_description = var.AMI_DESCRIPTION
  instance_type   = var.INSTANCE_TYPE
  region          = var.REGION
  subnet_id       = var.SUBNET_ID

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = [var.OWNER_ID] # Canonical’s official AWS account
  }

  ssh_username = var.SSH_USERNAME

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = var.VOLUME_SIZE
    volume_type           = var.VOLUME_TYPE
  }

  ami_users = [var.AMI_USER]
}


source "googlecompute" "ubuntu" {
  project_id        = var.GCP_PROJECT_ID
  zone              = var.GCP_ZONE
  machine_type      = var.GCP_MACHINE_TYPE
  image_name        = "${var.GCP_IMAGE_NAME}_${formatdate("YYYY_MM_DD_HH_mm_ss", timestamp())}"
  image_family      = "csye6225-health-checker-family"
  image_description = var.GCP_IMAGE_DESCRIPTION

  # Use the latest Ubuntu 24.04 LTS image
  source_image_family = "ubuntu-2404-lts"

  disk {
    size = var.GCP_DISK_SIZE
    type = var.GCP_DISK_TYPE
  }

  ssh_username = var.SSH_USERNAME
}


build {
  sources = [
    "source.amazon-ebs.my-ami",
    "source.googlecompute.ubuntu"
  ]


  provisioner "shell" {
    environment_vars = [
      "DB_URL=${var.DB_URL}",
      "DB_USRNAME=${var.DB_USRNAME}",
      "DB_PASSWORD=${var.DB_PASSWORD}",
      "DB_NAME=${var.DB_NAME}"
    ]
    script = "scripts/pre-setup.sh"
  }

  provisioner "file" {
    source      = "../target/health-checker-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/"
  }

  provisioner "file" {
    source      = "csye6225.service"
    destination = "/tmp/"
  }

  provisioner "shell" {
    script = "scripts/systemd-setup.sh"
  }

  post-processor "manifest" {
    output = "manifest.json"
  }
}