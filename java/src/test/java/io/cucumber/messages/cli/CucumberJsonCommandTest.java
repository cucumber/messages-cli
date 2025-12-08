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

class CucumberJsonCommandTest {

    static final Path minimalFeatureNdjson = Paths.get("../testdata/minimal.feature.ndjson");
    static final Path minimalFeatureXml = Paths.get("../testdata/cucumber-json/minimal.feature.json");

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
        int exitCode = cmd.execute("cucumber-json", "--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void writeToSystemOut() {
        var exitCode = cmd.execute("cucumber-json", "../testdata/minimal.feature.ndjson");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .isEqualTo(readString(minimalFeatureXml))
        );
    }

    @Test
    void failsToReadNonExistingFile() {
        var exitCode = cmd.execute("cucumber-json", "../testdata/no-such.feature.ndjson");
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid argument, could not read '../testdata/no-such.feature.ndjson'")
        );
    }

    @Test
    void readsFromSystemIn() throws IOException {
        System.setIn(newInputStream(minimalFeatureNdjson));
        var exitCode = cmd.execute("cucumber-json", "-");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(stdOut.toString())
                        .isEqualTo(readString(minimalFeatureXml))
        );
    }

    @Test
    void writesToOutputFile() {
        var destination = tmp.resolve("minimal.feature.json");
        var exitCode = cmd.execute("cucumber-json", "../testdata/minimal.feature.ndjson", "--output", destination.toString());
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .isEqualTo(readString(minimalFeatureXml))
        );
    }

    @Test
    void doesNotOverwriteWhenWritingToDirectory() {
        var exitCode1 = cmd.execute("cucumber-json", "../testdata/minimal.feature.ndjson", "--output", tmp.toString());
        var exitCode2 = cmd.execute("cucumber-json", "../testdata/minimal.feature.ndjson", "--output", tmp.toString());
        assertAll(
                () -> assertThat(exitCode1).isZero(),
                () -> assertThat(tmp.resolve("minimal.feature.json")).exists(),
                () -> assertThat(exitCode2).isZero(),
                () -> assertThat(tmp.resolve("minimal.feature.1.json")).exists()
        );
    }

    @Test
    void failsToWriteToReadOnlyOutputFile() throws IOException {
        var destination = Files.createFile(tmp.resolve("minimal.feature.json"));
        var isReadOnly = destination.toFile().setReadOnly();
        assertThat(isReadOnly).isTrue();

        var exitCode = cmd.execute("cucumber-json", "../testdata/minimal.feature.ndjson", "--output", destination.toString());
        assertAll(
                () -> assertThat(exitCode).isEqualTo(2),
                () -> assertThat(stdErr.toString())
                        .contains("Invalid value '%s' for option '--output': Could not write to '%s'"
                                .formatted(destination, destination))
        );
    }

    @Test
    void writesFileToCurrentWorkingDirectory() throws IOException {
        var destination = Paths.get("minimal.feature.json");
        Files.deleteIfExists(destination);

        var exitCode = cmd.execute("cucumber-json", "../testdata/minimal.feature.ndjson", "--output");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(readString(destination))
                        .isEqualTo(readString(minimalFeatureXml))
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
