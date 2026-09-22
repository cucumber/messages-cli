package io.cucumber.messages.cli;

import io.cucumber.messages.NdjsonToMessageReader;
import io.cucumber.messages.types.Envelope;
import io.cucumber.query.NamingStrategy;
import io.cucumber.query.NamingStrategy.ExampleName;
import io.cucumber.testngxmlformatter.MessagesToTestngXmlWriter;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static io.cucumber.messages.cli.JsonUtil.deserializer;
import static io.cucumber.query.NamingStrategy.FeatureName.EXCLUDE;
import static io.cucumber.query.NamingStrategy.Strategy.LONG;

@Command(
        name = "testng-xml",
        description = "Converts Cucumber messages to TestNG XML",
        mixinStandardHelpOptions = true
)
final class TestngXmlCommand implements Callable<Integer> {

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
            names = "--output",
            arity = "0..1",
            paramLabel = "file",
            description = "The output file containing TestNG XML. " +
                    "If file is a directory, a new file be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.xml'. If the file is omitted " +
                    "the current working directory is used."
    )
    private @Nullable Path output;

    @Option(
            names = "--example-naming-strategy",
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
        var namingStrategy = NamingStrategy.strategy(LONG)
                .featureName(EXCLUDE)
                .exampleName(exampleNameStrategy)
                .build();

        try (var reader = new NdjsonToMessageReader(options.sourceInputStream(), deserializer());
             var writer = MessagesToTestngXmlWriter.builder()
                     .testNamingStrategy(namingStrategy)
                     .build(options.outputPrintWriter())
        ) {
            reader.lines().forEach(envelope -> writeTo(writer, envelope));
        }
        return 0;
    }

    private static void writeTo(MessagesToTestngXmlWriter writer, Envelope envelope) {
        try {
            writer.write(envelope);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
