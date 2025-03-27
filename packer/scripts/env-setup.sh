# Create the application directory if it does not already exist
sudo mkdir -p /opt/csye6225

# Move the application JAR file to the designated directory and set appropriate permissions
sudo mv /tmp/health-checker-0.0.1-SNAPSHOT.jar /opt/csye6225/
sudo chown -R csye6225:csye6225 /opt/csye6225/
sudo chmod -R 755 /opt/csye6225/

# # Move the systemd service configuration file to the appropriate location: /etc/systemd/system/
sudo mv /tmp/csye6225.service /etc/systemd/system/

# Set the ownership of the service file to csye6225
sudo chown csye6225: /etc/systemd/system/csye6225.service

# Reload systemd to apply changes and recognize the new service
sudo systemctl daemon-reload

# Enable the service to start automatically on system boot
sudo systemctl enable csye6225.service


sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc/
sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json



sudo systemctl enable amazon-cloudwatch-agent

echo "Packer cloudwatch config copied to ami..."