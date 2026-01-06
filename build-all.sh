#!/bin/bash

# =====================================================
# Insurance Application - Build All Services Script
# =====================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# List of all services
SERVICES=(
    "insurance-service-registry"
    "insurance-identity-service"
    "insurance-policy-service"
    "insurance-claims-service"
    "insurance-hospital-service"
    "insurance-billing-service"
    "insurance-notification-service"
    "insurance-api-gateway"
)

# Function to print colored output
print_header() {
    echo -e "\n${BLUE}=====================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=====================================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}→ $1${NC}"
}

# Function to build a single service
build_service() {
    local service=$1
    local service_dir="${SCRIPT_DIR}/${service}"
    
    if [ -d "$service_dir" ]; then
        print_info "Building ${service}..."
        
        if (cd "$service_dir" && mvn clean package -DskipTests -q); then
            print_success "${service} built successfully!"
            return 0
        else
            print_error "Failed to build ${service}"
            return 1
        fi
    else
        print_error "Directory not found: ${service_dir}"
        return 1
    fi
}

# Main execution
main() {
    print_header "Building All Insurance Microservices"
    
    local failed_services=()
    local success_count=0
    local total=${#SERVICES[@]}
    
    for service in "${SERVICES[@]}"; do
        if build_service "$service"; then
            ((success_count++))
        else
            failed_services+=("$service")
        fi
    done
    
    # Summary
    print_header "Build Summary"
    echo -e "Total Services: ${total}"
    echo -e "${GREEN}Successful: ${success_count}${NC}"
    echo -e "${RED}Failed: ${#failed_services[@]}${NC}"
    
    if [ ${#failed_services[@]} -gt 0 ]; then
        echo -e "\n${RED}Failed services:${NC}"
        for service in "${failed_services[@]}"; do
            echo -e "  - ${service}"
        done
        exit 1
    else
        print_success "All services built successfully!"
        echo -e "\n${YELLOW}Next step: Run 'docker-compose up --build' to start all services${NC}"
    fi
}

# Run main function
main "$@"
