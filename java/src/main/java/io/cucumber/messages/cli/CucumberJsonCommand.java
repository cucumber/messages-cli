package io.cucumber.messages.cli;

import io.cucumber.jsonformatter.CucumberJvmJson.JvmFeature;
import io.cucumber.jsonformatter.MessagesToJsonWriter;
import io.cucumber.messages.NdjsonToMessageReader;
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
import static io.cucumber.messages.cli.JsonUtil.serializer;

@Command(
        name = "cucumber-json",
        description = "Converts Cucumber messages to a Cucumber JSON report",
        mixinStandardHelpOptions = true
)
final class CucumberJsonCommand implements Callable<Integer> {

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
            description = "The output file containing Cucumber JSON. " +
                    "If file is a directory, a new file be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.json'. If the file is omitted " +
                    "the current working directory is used."
    )
    private Path output;

    private static String json(String fileName) {
        return fileName + ".json";
    }

    @Override
    public Integer call() throws IOException {
        var options = new CommonOptions(spec, source, output, CucumberJsonCommand::json);
        try (var reader = new NdjsonToMessageReader(options.sourceInputStream(), deserializer());
             var writer = MessagesToJsonWriter.builder(serializer(JvmFeature[].class)::writeValue)
                     .build(options.outputPrintWriter())
        ) {
            reader.lines().forEach(envelope -> {
                try {
                    writer.write(envelope);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        return 0;
    }


}
