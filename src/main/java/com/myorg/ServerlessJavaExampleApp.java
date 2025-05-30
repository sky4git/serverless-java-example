package com.myorg;

import java.util.Map;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class ServerlessJavaExampleApp {
        public static void main(final String[] args) {
                App app = new App();

                new ServerlessJavaExampleStack(app, "ServerlessJavaExampleStack", StackProps.builder()

                                .env(Environment.builder()
                                                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                                                .region(System.getenv("ap-southeast-2"))
                                                .build())
                                .tags(Map.of(
                                                "Project", "ServerlessJavaExample",
                                                "Environment", "Development",
                                                "CostCenter", "12345"))
                                .description("serverless-java-example stack for AWS CDK Java example")
                                .build());

                app.synth();
        }
}
