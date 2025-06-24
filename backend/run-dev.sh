#!/bin/bash

echo "=== Spring Boot Device Management System ==="
echo "Building and starting the application..."

# Build the application
echo "Building application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Start the application with dev profile for sample data
    echo "Starting application with dev profile..."
    echo "Sample users will be created:"
    echo "  Admin: username=admin, password=admin123"
    echo "  User:  username=user, password=user123"
    echo ""
    echo "API Documentation will be available at:"
    echo "  http://localhost:8080/swagger-ui.html"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo "================================"
    
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
else
    echo "Build failed. Please check the errors above."
    exit 1
fi
