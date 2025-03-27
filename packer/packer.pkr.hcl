packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}




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

# Configuration for amazon ami source
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



build {
  sources = [
    "source.amazon-ebs.aws_ami"
  ]

  provisioner "shell" {
    script = "scripts/pre-req.sh"
  }

  provisioner "file" {
    source      = "../target/health-checker-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/"
  }

  provisioner "file" {
    source      = "csye6225.service"
    destination = "/tmp/"
  }
  provisioner "file" {
    source      = "cloudwatch-config.json"
    destination = "/tmp/"
  }


  provisioner "shell" {
    script = "scripts/env-setup.sh"
  }

  post-processor "manifest" {
    output = "manifest.json"
  }
}