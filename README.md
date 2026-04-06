# messages-cli

Commandline interface to work with Cucumber Messages

Given a messages file produce a test report in one of these formats:

* [JUnit XML](https://github.com/cucumber/junit-xml-formatter)
* [TestNG XML](https://github.com/cucumber/testng-xml-formatter)
* [Cucumber HTML](https://github.com/cucumber/html-formatter/)
* [Cucumber JSON](https://github.com/cucumber/cucumber-json-formatter)

## Usage

See: `cucumber-messages --help`

## Installation

You can install messages-cli using the package manager of your choice:

| Package manager | Platform | Installation                                  | Completions |
|-----------------|----------|-----------------------------------------------|-------------|
| **Snap**        | 🐧       | `snap install cucumber-messages`              | Bash / Zsh  |
| **Homebrew**    | 🍎 🐧    | `brew install cucumber/tap/cucumber-messages` | ️           |
| **Chocolatey**  | 🪟       | `choco install cucumber-messages`             |             |

The application ships with a completions file for bash and zsh. They're
automatically enabled when installed through Snap. Please let me know if you
know how to enable completions for Homebrew. 
