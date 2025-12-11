# messages-cli

Commandline interface to work with Cucumber Messages

Given a messages file produce a test report in one of these format: 

* JUnit XML
* TestNG XML
* Cucumber HTML
* Cucumber JSON

## Building

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

Then install the artifacts, for example on Unix:

```shell
cp -r target/app/cucumber-messages-cli ~/opt
ln -s ~/opt/cucumber-messages-cli/bin/cucumber-messages ~/.local/bin/cucumber-messages
```

On Windows: copy the `target/app/cucumber-messages-cli` somewhere and put the
`cucumber-messages.bat` in your `PATH`.

## Usage

See: `cucumber-messages --help`