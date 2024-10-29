package io.cucumber.messages.cli;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Objects.requireNonNull;

final class CommonOptions {
    private final CommandSpec spec;
    private final Path source;
    private final Path output;
    private final BiFunction<String, Integer, String> fileNameGenerator;


    CommonOptions(CommandSpec spec, Path source, Path output, BiFunction<String, Integer, String> fileNameGenerator) {
        this.spec = requireNonNull(spec);
        this.source = requireNonNull(source);
        this.output = output;
        this.fileNameGenerator = requireNonNull(fileNameGenerator);

        if (isSourceSystemIn()) {
            if (isDestinationDirectory()) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        ("Invalid value '%s' for option '--output': When " +
                                "reading from standard input, output can not " +
                                "be a directory").formatted(output)
                );
            }
        }

    }

    private boolean isSourceSystemIn() {
        var fileName = source.getFileName();
        return fileName != null && fileName.toString().equals("-");
    }

    private boolean isDestinationDirectory() {
        return output != null && Files.isDirectory(output);
    }

    OutputStream outputPrintWriter() {
        if (output == null) {
            return System.out;
        }

        Path path = outputPath();
        try {
            return newOutputStream(path, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    ("Invalid value '%s' for option '--output': Could not " +
                            "write to '%s'"
                    ).formatted(output, path), e);
        }
    }

    private Path outputPath() {
        if (!isDestinationDirectory()) {
            return output;
        }

        // Given a directory, decide on a file name
        var fileName = source.getFileName().toString();
        var index = fileName.lastIndexOf(".");
        if (index >= 0) {
            fileName = fileName.substring(0, index);
        }
        var candidate = output.resolve(fileName + ".xml");

        // Avoid overwriting existing files when we decided the file name.
        var counter = 1;
        while (Files.exists(candidate)) {
            candidate = output.resolve(fileNameGenerator.apply(fileName, counter++));
        }
        return candidate;
    }

    InputStream sourceInputStream() {
        if (isSourceSystemIn()) {
            return System.in;
        }
        try {
            return Files.newInputStream(source);
        } catch (IOException e) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid argument, could not read '%s'".formatted(source), e);
        }
    }
}
