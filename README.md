# Welcome to your CDK Java project!

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

- `mvn package` compile and run tests
- `cdk ls` list all stacks in the app
- `cdk synth` emits the synthesized CloudFormation template
- `cdk deploy` deploy this stack to your default AWS account/region
- `cdk diff` compare deployed stack with current state
- `cdk docs` open CDK documentation

Enjoy!

## Deploymnt requirements

- Java 21
- AWS CLI profile configured in your local machine

# Deploy

Run `aws sso login` in your terminal if you are not logged in with aws cli.

Deploy using `sh deploy.sh`
