package io.cucumber.messages.cli;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.MessageToNdjsonWriter;
import io.cucumber.messages.types.Envelope;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "gherkin",
        description = "Converts a Gherkin Document to Cucumber Messages",
        mixinStandardHelpOptions = true
)
class GherkinCommand implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(
            index = "0",
            paramLabel = "file",
            description = "The input file containing a Gherkin document. " +
                    "Use - to read from the standard input."
    )
    private Path source;

    @Option(
            names = {"-o", "--output"},
            arity = "0..1",
            paramLabel = "file",
            description = "The output file containing Cucumber Messages. " +
                    "If file is a directory, a new file be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.ndjson'. If the file is omitted " +
                    "the current working directory is used."
    )
    private Path output;

    @Option(
            names = {"-s", "--include-source"},
            description = "Includes the Source message in the output"
    )
    private boolean includeSource;

    @Option(
            names = {"-d", "--no-include-gherkin-document"},
            description = "Excludes the GherkinDocument message from the output"
    )
    private boolean excludeGherkinDocument;
    @Option(
            names = {"-p", "--include-pickles"},
            description = "Includes the Pickle messages in the output"
    )
    private boolean includePickles;

    private static String ndjson(String fileName) {
        return fileName + ".ndjson";
    }

    @Override
    public Integer call() throws IOException {
        var options = new CommonOptions(spec, source, output, GherkinCommand::ndjson);

        var parser = GherkinParser.builder()
                .includeSource(includeSource)
                .includeGherkinDocument(!excludeGherkinDocument)
                .includePickles(includePickles)
                .build();

        var uri = options.isSourceSystemIn() ? "file:///dev/fd/0" : source.toUri().toString();
        try (var writer = new MessageToNdjsonWriter(options.outputPrintWriter(), Jackson.serializer())) {
            parser.parse(uri, options.sourceInputStream())
                    .forEach(envelope -> write(writer, envelope));
        }
        return 0;
    }

    private static void write(MessageToNdjsonWriter writer, Envelope envelope) {
        try {
            writer.write(envelope);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
