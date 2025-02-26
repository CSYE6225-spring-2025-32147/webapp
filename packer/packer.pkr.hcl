packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}


variable "DB_URL" { type = string }
variable "DB_USRNAME" { type = string }
variable "DB_PASSWORD" { type = string }
variable "DB_NAME" { type = string }
variable "PRT_NBR" { type = string }
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

# Amazon AMI Source Configuration
source "amazon-ebs" "my-ami" {
  ami_name        = "${var.AMI_NAME}_${formatdate("YYYY_MM_DD_HH_MM_SS", timestamp())}"
  ami_description = var.AMI_DESCRIPTION
  instance_type   = var.instance_type
  region          = var.region
  subnet_id       = var.subnet_id

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = [var.OWNER_ID] # Canonical’s official AWS account
  }

  ssh_username = var.ssh_username

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = var.VOLUME_SIZE
    volume_type           = var.VOLUME_TYPE
  }
}


build {
  sources = ["source.amazon-ebs.my-ami"]

  provisioner "shell" {
    environment_vars = [
      "DB_URL=${var.DB_URL}",
      "DB_USRNAME=${var.DB_USRNAME}",
      "DB_PASSWORD=${var.DB_PASSWORD}",
      "DB_NAME=${var.DB_NAME}",
      "PRT_NBR=${var.PRT_NBR}"
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