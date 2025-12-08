package io.cucumber.messages.cli;

import io.cucumber.messages.NdjsonToMessageIterable;
import io.cucumber.query.NamingStrategy.ExampleName;
import io.cucumber.testngxmlformatter.MessagesToTestngXmlWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "testng-xml",
        description = "Converts Cucumber messages to TestNG XML",
        mixinStandardHelpOptions = true
)
class TestngXmlCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(
            index = "0",
            paramLabel = "file",
            description = "The input file containing Cucumber messages. " +
                    "Use - to read from the standard input."
    )
    private Path source;

    @Option(
            names = {"--output"},
            arity = "0..1",
            paramLabel = "file",
            description = "The output file containing JUnit XML. " +
                    "If file is a directory, a new file be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.xml'. If the file is omitted " +
                    "the current working directory is used."
    )
    private Path output;

    @Option(
            names = {"--example-naming-strategy"},
            paramLabel = "strategy",
            description = "How to name examples. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "NUMBER_AND_PICKLE_IF_PARAMETERIZED"
    )
    private ExampleName exampleNameStrategy;

    private static String xml(String fileName) {
        return fileName + ".xml";
    }

    @Override
    public Integer call() throws IOException {
        var options = new CommonOptions(spec, source, output, TestngXmlCommand::xml);

        try (var envelopes = new NdjsonToMessageIterable(options.sourceInputStream(), Jackson.deserializer());
             var writer = new MessagesToTestngXmlWriter(exampleNameStrategy, options.outputPrintWriter())
        ) {
            for (var envelope : envelopes) {
                writer.write(envelope);
            }
        }
        return 0;
    }


}
