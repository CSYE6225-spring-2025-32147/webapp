#!/bin/bash

# Exit on any error
set -e

# Define variables
DB_NAME="health_checker_db"
GROUP_NAME="csye6225group"
USER_NAME=“surya”
APP_DIR="/opt/csye6225"
ARCHIVE_PATH="/surya/ChinnasuryaPrasad_Vulavala_002056815_02.zip"  # Updated path to the zip file

# Update package lists and upgrade packages
echo "Updating package lists and upgrading packages..."
sudo apt update && sudo apt upgrade -y

# Install MySQL Server
echo "Installing MySQL Server..."
sudo apt install -y mysql-server

# Start and enable MySQL service
sudo systemctl enable --now mysql

# Secure MySQL installation (optional)
# sudo mysql_secure_installation

# Create database (without creating a user)
echo "Creating MySQL database..."
sudo mysql -e "CREATE DATABASE IF NOT EXISTS \⁠ ${DB_NAME}\ ⁠;"

# Create a new group
echo "Creating group ${GROUP_NAME}..."
sudo groupadd -f $GROUP_NAME

# Create a new user and add to the group
echo "Creating user ${USER_NAME}..."
sudo useradd -m -s /bin/bash -g $GROUP_NAME $USER_NAME || echo "User already exists."

# Ensure /opt/csye6225 exists
sudo mkdir -p $APP_DIR

# Unzip application to /opt/csye6225
echo "Extracting application files..."
sudo apt install -y unzip
sudo unzip -o $ARCHIVE_PATH -d $APP_DIR

# Set permissions
echo "Setting permissions..."
sudo chown -R $USER_NAME:$GROUP_NAME $APP_DIR
sudo chmod -R 750 $APP_DIR

echo "MySQL setup and application deployment completed successfully!"