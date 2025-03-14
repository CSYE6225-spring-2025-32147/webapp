#!/bin/bash

# Exit immediately if any command fails
set -e

# Define required variables
DB_NAME="health_checker_db"
GROUP_NAME="csye6225"
USER_NAME="surya"
APP_DIR="/opt/csye6225"
ARCHIVE_PATH="/tmp/ChinnasuryaPrasad_Vulavala_002056815_02.zip"

# Update system packages
echo "Updating system packages to the latest versions..."
sudo apt update && sudo apt upgrade -y

# Install MySQL Server if not already installed
echo "Installing MySQL Server if not present..."
sudo apt install -y mysql-server

# Ensure MySQL service is running and enabled at startup
echo "Starting and enabling MySQL service..."
sudo systemctl enable --now mysql

# Create the database if it does not exist
echo "Creating database '${DB_NAME}' (if not already created)..."
sudo mysql -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME};"

# Ensure the user group exists
echo "Ensuring group '${GROUP_NAME}' exists..."
sudo groupadd -f $GROUP_NAME

# Create user if not already created and assign it to the group
echo "Ensuring user '${USER_NAME}' exists and is assigned to group '${GROUP_NAME}'..."
sudo useradd -m -s /bin/bash -g $GROUP_NAME $USER_NAME || echo "User '${USER_NAME}' already exists."

# Ensure the application directory exists
echo "Creating application directory '${APP_DIR}' if it does not exist..."
sudo mkdir -p $APP_DIR

# Install unzip if not installed and extract application files
echo "Installing unzip package if required and extracting application archive..."
sudo apt install -y unzip
sudo unzip -o $ARCHIVE_PATH -d $APP_DIR

# Assign appropriate ownership and permissions to the application directory
echo "Setting appropriate ownership and permissions for '${APP_DIR}'..."
sudo chown -R $USER_NAME:$GROUP_NAME $APP_DIR
sudo chmod -R 750 $APP_DIR

echo "Database setup and application deployment completed successfully!"