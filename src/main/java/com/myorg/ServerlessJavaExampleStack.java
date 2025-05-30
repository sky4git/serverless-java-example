package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.LayerVersion;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;

import java.util.List;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.RemovalPolicy;

public class ServerlessJavaExampleStack extends Stack {
        public ServerlessJavaExampleStack(final Construct scope, final String id) {
                this(scope, id, null);
        }

        public ServerlessJavaExampleStack(final Construct scope, final String id, final StackProps props) {
                super(scope, id, props);

                // Create a Layer for Shared code
                LayerVersion sharedLayer = LayerVersion.Builder.create(this, "SharedLayer")
                                .layerVersionName("SharedLayer")
                                .code(Code.fromAsset("lambda/shared/target/shared-1.0.0-shaded.jar"))
                                .compatibleRuntimes(List.of(Runtime.JAVA_21))
                                .description("Shared code layer for Lambda functions")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .build();

                // Lambda for Task 1
                // Write an API that returns a list of all the available restaurant deals
                // that are active at a specified time of day
                Function getRestaurantDealsByTime = Function.Builder.create(this, "GetRestaurantDealsByTime")
                                .functionName("GetRestaurantDealsByTime")
                                .runtime(Runtime.JAVA_21)
                                .code(Code.fromAsset(
                                                "lambda/getRestaurantDealsByTime/target/getRestaurantDealsByTime-1.0.0.jar"))
                                .handler("com.myorg.GetRestaurantDealsByTime::handleRequest")
                                .memorySize(128)
                                .timeout(Duration.seconds(30))
                                .layers(List.of(sharedLayer))
                                .environment(Map.of(
                                                "RESTAURANTS_API_URL",
                                                "https://eccdn.com.au/misc/challengedata.json"))
                                .description("Lambda function to fetch restaurant deals by time")
                                .build();

                // Lambda for Task 2
                // Write an API that returns the peak time window for restaurant deals
                Function getPeakTime = Function.Builder.create(this, "GetPeakTime")
                                .functionName("GetPeakTime")
                                .runtime(Runtime.JAVA_21)
                                .code(Code.fromAsset("lambda/getPeakTime/target/getPeakTime-1.0.0.jar"))
                                .handler("com.myorg.GetPeakTime::handleRequest")
                                .memorySize(128)
                                .timeout(Duration.seconds(30))
                                .layers(List.of(sharedLayer))
                                .environment(Map.of(
                                                "RESTAURANTS_API_URL",
                                                "https://eccdn.com.au/misc/challengedata.json"))
                                .description("Lambda function to fetch peak time")
                                .build();

                // Define HTTP API Gateway v2 and integrate with Lambda
                HttpLambdaIntegration dealsIntegration = new HttpLambdaIntegration(
                                "DealsIntegration", getRestaurantDealsByTime);
                HttpLambdaIntegration peakTimeIntegration = new HttpLambdaIntegration("PeakTimeIntegration",
                                getPeakTime);
                // Create the HTTP API
                HttpApi httpApi = HttpApi.Builder.create(this, "RestaurantDealsHttpApi")
                                .apiName("RestaurantDealsHttpApi")
                                .build();

                // Add a route to the API Gateway for the `getRestaurantDealsByTime` Lambda
                // function
                httpApi.addRoutes(AddRoutesOptions.builder()
                                .path("/deals")
                                .methods(List.of(HttpMethod.GET))
                                .integration(dealsIntegration)
                                .build());

                // Add a route to the API Gateway for the `getPeakTime` Lambda function
                httpApi.addRoutes(AddRoutesOptions.builder()
                                .path("/peaktime")
                                .methods(List.of(HttpMethod.GET))
                                .integration(peakTimeIntegration)
                                .build());
        }
}
