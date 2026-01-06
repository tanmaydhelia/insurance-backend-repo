BASE_DIR=$(pwd)


gnome-terminal --tab --title="IDENTITY" -- bash -c "cd $BASE_DIR/insurance-identity-service && mvn clean package:run; exec bash"
gnome-terminal --tab --title="POLICY" -- bash -c "cd $BASE_DIR/insurance-policy-service && mvn clean package:run; exec bash"
gnome-terminal --tab --title="CLAIMS" -- bash -c "cd $BASE_DIR/insurance-claims-service && mvn clean package:run; exec bash"
gnome-terminal --tab --title="GATEWAY" -- bash -c "cd $BASE_DIR/insurance-api-gateway && mvn clean package:run; exec bash"
gnome-terminal --tab --title="HOSPITAL" -- bash -c "cd $BASE_DIR/insurance-hospital-service && mvn clean package:run; exec bash"
gnome-terminal --tab --title="BILLING" -- bash -c "cd $BASE_DIR/insurance-billing-service && mvn clean package:run; exec bash"
gnome-terminal --tab --title="NOTIFICATION" -- bash -c "cd $BASE_DIR/insurance-notification-service && mvn clean package:run; exec bash"

echo "All tabs initialized."