package io.cucumber.messages.cli;

import io.cucumber.messages.MessageToNdjsonWriter;
import io.cucumber.messages.NdjsonToMessageReader;
import io.cucumber.messages.types.Attachment;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.ExternalAttachment;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;

import static io.cucumber.messages.cli.JsonUtil.deserializer;
import static io.cucumber.messages.cli.JsonUtil.serializer;
import static java.nio.charset.StandardCharsets.UTF_8;

@Command(
        name = "externalize-attachments",
        description = "Externalizes attachments embedded in messages",
        mixinStandardHelpOptions = true
)
final class ExternalizeAttachmentsCommand implements Callable<Integer> {

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
            names = "--output-directory",
            arity = "1",
            paramLabel = "directory",
            description = "The output directory for the externalized attachments."
    )
    private Path outputDirectory;


    @Option(
            names = "--output",
            arity = "0..1",
            paramLabel = "file",
            description = "The output file containing the externalized messages. " +
                    "If file is a directory, a new file will be " +
                    "created by taking the name of the input file and " +
                    "replacing the suffix with '.externalized.ndjson'. If the file is omitted " +
                    "the current working directory is used."
    )
    private @Nullable Path output;

    private static String log(String fileName) {
        return fileName + ".externalized.ndjson";
    }

    @Override
    public Integer call() throws IOException {
        if (!Files.exists(outputDirectory)) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid option: --output-directory must exist");
        }
        if (!Files.isDirectory(outputDirectory)) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid option: --output-directory must be a directory");
        }
        var options = new CommonOptions(spec, source, output, ExternalizeAttachmentsCommand::log);

        try (var reader = new NdjsonToMessageReader(options.sourceInputStream(), deserializer());
             var writer = new MessageToNdjsonWriter(options.outputPrintWriter(), serializer(Envelope.class)::writeValue)
        ) {
            reader.lines().forEach(envelope -> writeTo(writer, replaceAttachment(envelope)));
        }
        return 0;
    }

    private Envelope replaceAttachment(Envelope envelope) {
        return envelope.getAttachment()
                .map(this::externalize)
                .map(Envelope::of)
                .orElse(envelope);
    }

    private ExternalAttachment externalize(Attachment attachment) {
        var fileName = attachment.getFileName().orElseGet(() -> UUID.randomUUID().toString());
        var file = outputDirectory.resolve(fileName);
        var content = switch (attachment.getContentEncoding()) {
            case IDENTITY -> attachment.getBody().getBytes(UTF_8);
            case BASE64 -> Base64.getDecoder().decode(attachment.getBody());
        };

        try {
            Files.write(file, content, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return new ExternalAttachment(
                file.toUri().toString(),
                attachment.getMediaType(),
                attachment.getTestCaseStartedId().orElse(null),
                attachment.getTestStepId().orElse(null),
                attachment.getTestRunHookStartedId().orElse(null),
                attachment.getTimestamp().orElse(null));
    }

    private static void writeTo(MessageToNdjsonWriter writer, Envelope envelope) {
        try {
            writer.write(envelope);
        } catch (IOException e1) {
            throw new UncheckedIOException(e1);
        }
    }

}
