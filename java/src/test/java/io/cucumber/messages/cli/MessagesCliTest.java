package io.cucumber.messages.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MessagesCliTest {

    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();
    final CommandLine cmd = MessagesCli.createCommandLine();
    PrintStream originalSystemOut;


    @BeforeEach
    void setup() {
        cmd.setErr(new PrintWriter(err));
        cmd.setOut(new PrintWriter(out));
    }

    @AfterEach
    void cleanup() {
        // Helps with debugging
        System.out.println("Contents of out:");
        System.out.println(out);
        System.out.println("Contents of err:");
        System.out.println(err);
    }

    @Test
    void help() {
        int exitCode = cmd.execute("--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void version() {
        var exitCode = cmd.execute("--version");
        assertAll(
                () -> assertThat(exitCode).isZero(),
                () -> assertThat(out.toString())
                        .isEqualToIgnoringNewLines("messages-cli DEVELOPMENT")
        );
    }

}
