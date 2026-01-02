BASE_DIR=$(pwd)

echo "Starting Microservices in Tabs..."

gnome-terminal --tab --title="REGISTRY" -- bash -c "cd $BASE_DIR/insurance-service-registry && mvn spring-boot:run; exec bash"

echo "Waiting 15s for Registry..."
sleep 15

gnome-terminal --tab --title="IDENTITY" -- bash -c "cd $BASE_DIR/insurance-identity-service && mvn spring-boot:run; exec bash"
gnome-terminal --tab --title="POLICY" -- bash -c "cd $BASE_DIR/insurance-policy-service && mvn spring-boot:run; exec bash"
gnome-terminal --tab --title="CLAIMS" -- bash -c "cd $BASE_DIR/insurance-claims-service && mvn spring-boot:run; exec bash"
gnome-terminal --tab --title="GATEWAY" -- bash -c "cd $BASE_DIR/insurance-api-gateway && mvn spring-boot:run; exec bash"

echo "All tabs initialized."