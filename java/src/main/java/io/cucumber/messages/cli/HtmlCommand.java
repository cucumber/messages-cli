package io.cucumber.messages.cli;

import io.cucumber.htmlformatter.MessagesToHtmlWriter;
import io.cucumber.jsonformatter.MessagesToJsonWriter;
import io.cucumber.messages.NdjsonToMessageIterable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "html",
        description = "Converts Cucumber messages to a HTML report",
        mixinStandardHelpOptions = true
)
final class HtmlCommand implements Callable<Integer> {

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
            description = "The output file containing the HTML report. " +
                    "If file is a directory, a new file be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.html'. If the file is omitted " +
                    "the current working directory is used."
    )
    private Path output;

    private static String html(String fileName) {
        return fileName + ".html";
    }

    @Override
    public Integer call() throws IOException {
        var options = new CommonOptions(spec, source, output, HtmlCommand::html);

        try (var envelopes = new NdjsonToMessageIterable(options.sourceInputStream(), Jackson.deserializer());
             var writer = MessagesToHtmlWriter.builder(Jackson.OBJECT_MAPPER::writeValue).build(options.outputPrintWriter())
        ) {
            for (var envelope : envelopes) {
                writer.write(envelope);
            }
        }
        return 0;
    }


}
