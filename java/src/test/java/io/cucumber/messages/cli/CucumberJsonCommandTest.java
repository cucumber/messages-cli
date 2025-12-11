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

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CucumberJsonCommandTest {

    static final Path minimalFeatureNdjson = Paths.get("../testdata/compatibility-kit/src/minimal.ndjson");
    static final Path minimalFeatureJson = Paths.get("../testdata/cucumber-json/minimal.json");

    final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    final StringWriter stdErr = new StringWriter();
    CommandLine cmd;
    InputStream originalSystemIn;
    PrintStream originalSystemOut;

    @TempDir
    Path tmp;

    @BeforeEach
    void setup() {
        cmd = CucumberMessagesCli.createCommandLine();
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
    }

    @Test
    void help() {
        int exitCode = cmd.execute("cucumber-json", "--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void writeToSystemOut() {
        var exitCode = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/minimal.ndjson");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .isEqualTo(readString(minimalFeatureJson))
        );
    }

    @Test
    void failsToReadNonExistingFile() {
        var exitCode = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/no-such.ndjson");
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid argument, could not read '../testdata/compatibility-kit/src/no-such.ndjson'")
        );
    }

    @Test
    void readsFromSystemIn() throws IOException {
        System.setIn(newInputStream(minimalFeatureNdjson));
        var exitCode = cmd.execute("cucumber-json", "-");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .isEqualTo(readString(minimalFeatureJson))
        );
    }

    @Test
    void writesToOutputFile() {
        var destination = tmp.resolve("minimal.json");
        var exitCode = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/minimal.ndjson", "--output", destination.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .isEqualTo(readString(minimalFeatureJson))
        );
    }

    @Test
    void doesNotOverwriteWhenWritingToDirectory() {
        var exitCode1 = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/minimal.ndjson", "--output", tmp.toString());
        var exitCode2 = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/minimal.ndjson", "--output", tmp.toString());
        assertAll(
                () -> assertThat(exitCode1).isZero(),
                () -> assertThat(tmp.resolve("minimal.json")).exists(),
                () -> assertThat(exitCode2).isZero(),
                () -> assertThat(tmp.resolve("minimal.1.json")).exists()
        );
    }

    @Test
    void failsToWriteToReadOnlyOutputFile() throws IOException {
        var destination = Files.createFile(tmp.resolve("minimal.json"));
        var isReadOnly = destination.toFile().setReadOnly();
        assertThat(isReadOnly).isTrue();

        var exitCode = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/minimal.ndjson", "--output", destination.toString());
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '%s' for option '--output': Could not write to '%s'"
                                .formatted(destination, destination))
        );
    }

    @Test
    void writesFileToCurrentWorkingDirectory() throws IOException {
        var destination = Paths.get("minimal.json");
        Files.deleteIfExists(destination);

        var exitCode = cmd.execute("cucumber-json", "../testdata/compatibility-kit/src/minimal.ndjson", "--output");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .isEqualTo(readString(minimalFeatureJson))
        );
        Files.deleteIfExists(destination);
    }

    @Test
    void canNotGuessFileNameWhenReadingFromSystemIn() throws IOException {
        System.setIn(newInputStream(minimalFeatureNdjson));
        var exitCode = cmd.execute("cucumber-json", "-", "--output");
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '' for option '--output': When reading from standard input, output can not be a directory")
        );
    }

}
