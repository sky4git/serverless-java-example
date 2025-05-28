package com.myorg;

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

                                // For more information, see
                                // https://docs.aws.amazon.com/cdk/latest/guide/environments.html
                                .build());

                app.synth();
        }
}
