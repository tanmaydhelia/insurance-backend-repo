#!/bin/bash

# SonarCloud Analysis Script for Insurance Microservices
# Usage: ./sonar-analysis.sh [service-name]
# If no service name provided, all services will be analyzed

# Set the SONAR_TOKEN environment variable
export SONAR_TOKEN="33f941e09359682ed6a7bb90025ed8ae5bc1000c"
SONAR_HOST="https://sonarcloud.io"
BASE_DIR="/home/tanmayy-2312/Desktop/Spring_Tool_WorkSpace/Capstone_Smart-health-insurance"

# Services to analyze
SERVICES=(
    "insurance-identity-service"
    "insurance-policy-service"
    "insurance-claims-service"
    "insurance-hospital-service"
)

analyze_service() {
    local service=$1
    echo "=============================================="
    echo "Analyzing: $service"
    echo "=============================================="
    
    cd "$BASE_DIR/$service" || exit 1
    
    # Run verify (tests + coverage) and SonarCloud analysis
    echo "Running tests with coverage and SonarCloud analysis..."
    mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
        -Dsonar.projectKey=tanmaydhelia_$service
    
    echo "✅ $service analysis complete!"
    echo ""
}

# If specific service provided
if [ -n "$1" ]; then
    analyze_service "$1"
else
    # Analyze all services
    for service in "${SERVICES[@]}"; do
        analyze_service "$service"
    done
fi

echo "=============================================="
echo "All SonarCloud analyses complete!"
echo "View reports at: $SONAR_HOST"
echo "Organization: tanmaydhelia"
echo "=============================================="

# USE THIS
# cd /home/tanmayy-2312/Desktop/Spring_Tool_WorkSpace/Capstone_Smart-health-insurance/insurance-identity-service && SONAR_TOKEN="33f941e09359682ed6a7bb90025ed8ae5bc1000c" mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=tanmaydhelia_insurance-identity-service -Dsonar.organization=tanmaydhelia -Dsonar.host.url=https://sonarcloud.io 2>&1 | tail -50