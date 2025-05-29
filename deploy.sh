#!/bin/bash
set -e

# Navigate to function-one and run tests
cd lambda/getRestaurantDealsByTime
mvn package
cd ..

# Navigate to the CDK project and deploy
cd ..
cdk deploy
