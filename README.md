# messages-cli

Commandline interface to work with Cucumber Messages

Given a messages file produce a test report in one of these format: 

* [JUnit XML](https://github.com/cucumber/junit-xml-formatter)
* [TestNG XML](https://github.com/cucumber/testng-xml-formatter)
* [Cucumber HTML](https://github.com/cucumber/html-formatter/)
* [Cucumber JSON](https://github.com/cucumber/cucumber-json-formatter)

## Building & Installing

Required
* Apache Maven 3.9.9+
* Java JRE 21+

First, checkout the latest release `X.Y.Z`: 

```
git clone https://github.com/cucumber/messages-cli.git
git checkout vX.Y.Z
```

Then build the project

```shell
cd java 
mvn clean package
```

Then install the artifacts, for example:

```shell
cp -r ./target/maven-jlink/default/* ~/opt/cucumber-messages-cli
ln -s ~/opt/cucumber-messages-cli/bin/cucumber-messages ~/.local/bin/cucumber-messages
```

## Usage

See: `cucumber-messages --help`