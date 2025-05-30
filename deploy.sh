#!/bin/bash
set -e

# Navigate to lambda layer and run tests
cd lambda/shared
mvn install
cd ..

# Navigate to function getRestaurantDealsByTime and run tests
cd getRestaurantDealsByTime
mvn clean package
cd ..

# Navigate to function getPeakTime and run tests
cd getPeakTime
mvn clean package
cd ..

# Navigate to the CDK project and deploy
cd ..
cdk deploy
