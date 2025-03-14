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

# Database Configuration Variables
variable "database_url" { type = string }
variable "database_username" { type = string }
variable "database_password" { type = string }
variable "database_name" { type = string }

# SSH Configuration
variable "ssh_user" { type = string }

# AWS Configuration
variable "aws_subnet_id" { type = string }
variable "aws_instance_type" { type = string }
variable "aws_region" {
  type    = string
  default = "us-east-1"
}
variable "aws_ami_name" {
  type    = string
  default = "csye6225_health_checker"
}
variable "aws_ami_description" {
  type    = string
  default = "AMI for web-app"
}
variable "aws_volume_size" {
  type    = number
  default = 8
}
variable "aws_volume_type" {
  type    = string
  default = "gp3"
}
variable "aws_owner_id" {
  type    = string
  default = "099720109477"
}

variable "aws_ami_user" {
  type = string
}

# GCP Configuration
variable "gcp_project_id" {
  type    = string
  default = "gcp-dev-452117"
}
variable "gcp_zone" {
  type    = string
  default = "us-central1-a"
}
variable "gcp_machine_type" {
  type = string
}
variable "gcp_image_name" {
  type    = string
  default = "app-health-checker-gcp"
}
variable "gcp_image_description" {
  type    = string
  default = "Custom GCP Image for Application Deployment"
}
variable "gcp_disk_size" {
  type    = number
  default = 25
}
variable "gcp_disk_type" {
  type    = string
  default = "pd-balanced"
}
variable "gcp_demo_project_id" {
  type = string
}

# Amazon AMI Source Configuration
source "amazon-ebs" "aws_ami" {
  ami_name        = "${var.aws_ami_name}_${formatdate("YYYY_MM_DD_HH_mm_ss", timestamp())}"
  ami_description = var.aws_ami_description
  instance_type   = var.aws_instance_type
  region          = var.aws_region
  subnet_id       = var.aws_subnet_id

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = [var.aws_owner_id] # Canonical’s official AWS account
  }

  ssh_username = var.ssh_user

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = var.aws_volume_size
    volume_type           = var.aws_volume_type
  }

  ami_users = [var.aws_ami_user]
}

# GCP Image Configuration
source "googlecompute" "gcp_image" {
  project_id        = var.gcp_project_id
  zone              = var.gcp_zone
  machine_type      = var.gcp_machine_type
  image_name        = "${var.gcp_image_name}-${formatdate("YYYY-MM-DD-HH-mm-ss", timestamp())}"
  image_family      = "csye6225-health-checker-family"
  image_description = var.gcp_image_description

  # Use the latest Ubuntu 24.04 LTS image
  source_image_family = "ubuntu-minimal-2404-lts-amd64"

  disk_size = var.gcp_disk_size
  disk_type = var.gcp_disk_type

  ssh_username = var.ssh_user
}

# Build Configuration
build {
  sources = [
    "source.amazon-ebs.aws_ami",
    "source.googlecompute.gcp_image"
  ]

  provisioner "shell" {
    environment_vars = [
      "DATABASE_URL=${var.database_url}",
      "DATABASE_USERNAME=${var.database_username}",
      "DATABASE_PASSWORD=${var.database_password}",
      "DATABASE_NAME=${var.database_name}"
    ]
    script = "scripts/pre-req.sh"
  }

  provisioner "file" {
    source      = "../target/app-health-checker-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/"
  }

  provisioner "file" {
    source      = "csye6225.service"
    destination = "/tmp/"
  }

  provisioner "shell" {
    script = "scripts/env-setup.sh"
  }

  post-processor "manifest" {
    output = "manifest.json"
  }
}