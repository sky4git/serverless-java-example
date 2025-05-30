#!/bin/bash
set -e

echo "Starting deployment process..."
# Navigate to lambda layer and run tests
cd lambda/shared
mvn -q install
cd ..

# Navigate to function getRestaurantDealsByTime and run tests
cd getRestaurantDealsByTime
mvn -q clean package
cd ..

# Navigate to function getPeakTime and run tests
cd getPeakTime
mvn -q clean package
cd ..

# Navigate to the CDK project and deploy
cd ..
cdk deploy --profile specta
echo "Deployment process completed successfully."