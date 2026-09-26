package io.cucumber.messages.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CucumberExternalizeAttachmentsCommandTest {

    static final String attachmentsFeatureNdjson = "../testdata/compatibility-kit/src/minimal.ndjson";
    static final Path attachmentsFeatureNdjsonPath = Paths.get(attachmentsFeatureNdjson);

    final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    final StringWriter stdErr = new StringWriter();
    CommandLine cmd;
    InputStream originalSystemIn;
    PrintStream originalSystemOut;

    @TempDir
    Path tmp;
    Path outputDirectory;

    @BeforeEach
    void setup() throws Exception {
        cmd = CucumberMessagesCli.createCommandLine();
        // TODO: Use mocking, but has wrong type. Ask pico CLI for mock with PrintStream.
        originalSystemIn = System.in;
        originalSystemOut = System.out;
        System.setOut(new PrintStream(stdOut));
        cmd.setErr(new PrintWriter(stdErr));
        outputDirectory = tmp.resolve("output-directory");
        Files.createDirectory(outputDirectory);
    }

    @AfterEach
    void cleanup() {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
    }

    @Test
    void help() {
        int exitCode = cmd.execute("externalize-attachments", "--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void writeToSystemOut() {
        var exitCode = cmd.execute("externalize-attachments", attachmentsFeatureNdjson,  "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString(UTF_8))
                        .matches("^\\{.+}$")
        );
    }

    @Test
    void failsToReadNonExistingFile() {
        var exitCode = cmd.execute("externalize-attachments", "../testdata/compatibility-kit/src/no-such.ndjson", "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid argument, could not read '../testdata/compatibility-kit/src/no-such.ndjson'")
        );
    }

    @Test
    void readsFromSystemIn() throws IOException {
        System.setIn(newInputStream(attachmentsFeatureNdjsonPath));
        var exitCode = cmd.execute("externalize-attachments", "-", "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString(UTF_8))
                        .matches("^\\{.+}$")
        );
    }

    @Test
    void writesToOutputFile() {
        var destination = tmp.resolve("attachments.externalized.json");
        var exitCode = cmd.execute("externalize-attachments", attachmentsFeatureNdjson, "--output", destination.toString(), "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .matches("^\\{.+}$")
        );
    }

    @Test
    void doesNotOverwriteWhenWritingToDirectory() {
        var exitCode1 = cmd.execute("externalize-attachments", attachmentsFeatureNdjson, "--output", tmp.toString(), "--output-directory", outputDirectory.toString());
        var exitCode2 = cmd.execute("externalize-attachments", attachmentsFeatureNdjson, "--output", tmp.toString(), "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode1).isZero(),
                () -> assertThat(tmp.resolve("attachments.ndjson")).exists(),
                () -> assertThat(exitCode2).isZero(),
                () -> assertThat(tmp.resolve("attachments.1.ndjson")).exists()
        );
    }

    @Test
    void failsToWriteToReadOnlyOutputFile() throws IOException {
        var destination = Files.createFile(tmp.resolve("attachments.ndjson"));
        var isReadOnly = destination.toFile().setReadOnly();
        assertThat(isReadOnly).isTrue();

        var exitCode = cmd.execute("externalize-attachments", attachmentsFeatureNdjson, "--output", destination.toString(), "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '%s' for option '--output': Could not write to '%s'"
                                .formatted(destination, destination))
        );
    }

    @Test
    void writesFileToCurrentWorkingDirectory() throws IOException {
        var destination = Paths.get("minimal.log");
        Files.deleteIfExists(destination);

        var exitCode = cmd.execute("externalize-attachments", attachmentsFeatureNdjson, "--output", "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .startsWith("\nFeature: minimal")
        );
        Files.deleteIfExists(destination);
    }

    @Test
    void canNotGuessFileNameWhenReadingFromSystemIn() throws IOException {
        System.setIn(newInputStream(attachmentsFeatureNdjsonPath));
        var exitCode = cmd.execute("externalize-attachments", "-", "--output", "--output-directory", outputDirectory.toString());
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '' for option '--output': When reading from standard input, output can not be a directory")
        );
    }

}
