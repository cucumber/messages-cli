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

class GherkinCommandTest {

    static final Path minimalFeature = Paths.get("../testdata/minimal.feature");

    final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    final StringWriter stdErr = new StringWriter();
    CommandLine cmd;
    InputStream originalSystemIn;
    PrintStream originalSystemOut;

    @TempDir
    Path tmp;

    @BeforeEach
    void setup() {
        cmd = MessagesCli.createCommandLine();
        // TODO: Use mocking, but has wrong type. Ask pico CLI for mock with PrintStream.
        originalSystemIn = System.in;
        originalSystemOut = System.out;
        System.setOut(new PrintStream(stdOut));
        cmd.setErr(new PrintWriter(stdErr));
    }

    @AfterEach
    void cleanup() {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
        // Helps with debugging
        System.out.println(stdOut.toString(UTF_8));
        System.out.println(stdErr);
    }

    @Test
    void help() {
        int exitCode = cmd.execute("gherkin", "--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void writeToSystemOut() {
        var exitCode = cmd.execute("gherkin", minimalFeature.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .hasLineCount(1)
                        .startsWith("{\"gherkinDocument\":")
        );
    }

    @Test
    void failsToReadNonExistingFile() {
        var exitCode = cmd.execute("gherkin", "../testdata/no-such.feature");
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid argument, could not read '../testdata/no-such.feature'")
        );
    }

    @Test
    void readsFromSystemIn() throws IOException {
        System.setIn(newInputStream(minimalFeature));
        var exitCode = cmd.execute("gherkin", "-");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .hasLineCount(1)
                        .startsWith("{\"gherkinDocument\":")
        );
    }

    @Test
    void writesToOutputFile() {
        var destination = tmp.resolve("minimal.feature.ndjson");
        var exitCode = cmd.execute("gherkin", "../testdata/minimal.feature", "--output", destination.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .hasLineCount(1)
                        .startsWith("{\"gherkinDocument\":")
        );
    }

    @Test
    void doesNotOverwriteWhenWritingToDirectory() {
        var exitCode1 = cmd.execute("gherkin", "../testdata/minimal.feature", "--output", tmp.toString());
        var exitCode2 = cmd.execute("gherkin", "../testdata/minimal.feature", "--output", tmp.toString());
        assertAll(
                () -> assertThat(exitCode1).isZero(),
                () -> assertThat(tmp.resolve("minimal.ndjson")).exists(),
                () -> assertThat(exitCode2).isZero(),
                () -> assertThat(tmp.resolve("minimal.1.ndjson")).exists()
        );
    }

    @Test
    void failsToWriteToReadOnlyOutputFile() throws IOException {
        var destination = Files.createFile(tmp.resolve("minimal.feature"));
        var isReadOnly = destination.toFile().setReadOnly();
        assertThat(isReadOnly).isTrue();

        var exitCode = cmd.execute("gherkin", "../testdata/minimal.feature", "--output", destination.toString());
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '%s' for option '--output': Could not write to '%s'"
                                .formatted(destination, destination))
        );
    }

    @Test
    void writesFileToCurrentWorkingDirectory() throws IOException {
        var destination = Paths.get("minimal.ndjson");
        Files.deleteIfExists(destination);

        var exitCode = cmd.execute("gherkin", "../testdata/minimal.feature", "--output");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .hasLineCount(1)
                        .startsWith("{\"gherkinDocument\":")
        );

        Files.deleteIfExists(destination);
    }

    @Test
    void canNotGuessFileNameWhenReadingFromSystemIn() throws IOException {
        System.setIn(newInputStream(minimalFeature));
        var exitCode = cmd.execute("gherkin", "-", "--output");
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '' for option '--output': When reading from standard input, output can not be a directory")
        );
    }

    @Test
    void includeSource() {
        var exitCode = cmd.execute("gherkin", "--include-source", minimalFeature.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .hasLineCount(2)
                        .startsWith("{\"source\":")
                        .contains("{\"gherkinDocument\":")
        );
    }
    @Test
    void includePickles() {
        var exitCode = cmd.execute("gherkin", "--include-pickles", minimalFeature.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .hasLineCount(2)
                        .startsWith("{\"gherkinDocument\":")
                        .contains("{\"pickle\":")
        );
    }
    @Test
    void excludeDocument() {
        var exitCode = cmd.execute("gherkin", "--no-include-gherkin-document", "--include-source", minimalFeature.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .hasLineCount(1)
                        .startsWith("{\"source\":")
        );
    }

}
