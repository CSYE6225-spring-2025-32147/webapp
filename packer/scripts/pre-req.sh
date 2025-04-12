#!/bin/bash

# Exit on any error
set -e

export DEBIAN_FRONTEND=noninteractive

# Updating package lists...
sudo apt update -y && sudo apt upgrade -y

# Installing JDK 17...
sudo apt install -y openjdk-17-jdk

# Installing Maven
sudo apt install -y maven

# Installing jq
sudo apt install -y jq

sudo apt install -y unzip

# Installing AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
sudo ./aws/install
rm -rf aws awscliv2.zip

# Create the group csye6225 if it does not already exist
sudo groupadd -f csye6225

# Create the user csye6225 with the primary group csye6225 and no login shell
sudo useradd -m -g csye6225 -s /usr/sbin/nologin csye6225


echo "All the required dependencies are successfully installed "

# Install CloudWatch Agent
echo "Installing CloudWatch Agent..."
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb
rm amazon-cloudwatch-agent.deb
