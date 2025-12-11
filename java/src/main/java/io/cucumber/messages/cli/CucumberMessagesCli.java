package io.cucumber.messages.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "cucumber-messages",
        mixinStandardHelpOptions = true,
        headerHeading = "Work with Cucumber messages\n",
        versionProvider = VersionProvider.class,
        subcommands = {
                CucumberJsonCommand.class,
                HtmlCommand.class,
                JunitXmlCommand.class,
                TestngXmlCommand.class,
        }
)
final class CucumberMessagesCli {

    public static void main(String... args) {
        var commandLine = createCommandLine();
        var exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    static CommandLine createCommandLine() {
        return new CommandLine(new CucumberMessagesCli());
    }
}
