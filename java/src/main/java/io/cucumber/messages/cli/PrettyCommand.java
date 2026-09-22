package io.cucumber.messages.cli;

import io.cucumber.messages.NdjsonToMessageReader;
import io.cucumber.prettyformatter.MessagesToPrettyWriter;
import io.cucumber.prettyformatter.MessagesToSummaryWriter;
import io.cucumber.prettyformatter.Theme;
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

@Command(
        name = "pretty",
        description = "Converts Cucumber messages to richly formatted result on the terminal",
        mixinStandardHelpOptions = true
)
final class PrettyCommand implements Callable<Integer> {

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
            description = "The output file containing the pretty report. " +
                    "If file is a directory, a new file will be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.log'. If the file is omitted " +
                    "the current working directory is used."
    )
    private @Nullable Path output;

    private static String log(String fileName) {
        return fileName + ".log";
    }

    @Override
    public Integer call() throws IOException {
        var options = new CommonOptions(spec, source, output, PrettyCommand::log);

        try (var reader = new NdjsonToMessageReader(options.sourceInputStream(), deserializer());
             var out = options.outputPrintWriter();
             var prettyWriter = MessagesToPrettyWriter.builder()
                     .theme(theme())
                     .build(out);
             var summaryWriter = MessagesToSummaryWriter.builder()
                     .theme(theme())
                     .build(out);
        ) {

            reader.lines().forEach(envelope -> {
                try {
                    prettyWriter.write(envelope);
                    summaryWriter.write(envelope);
                } catch (IOException e1) {
                    throw new UncheckedIOException(e1);
                }
            });
        }
        return 0;
    }

    private Theme theme() {
        if (output != null) {
            return Theme.plain();
        }
        if (!spec.commandLine().getColorScheme().ansi().enabled()) {
            return Theme.plain();
        }
        return Theme.cucumber();
    }

}
