package io.cucumber.messages.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

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
                        .hasLineCount(4)
                        .matches(Pattern.compile("""
                                        gherkin \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        junit-xml-formatter \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        messages-cli \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        testng-xml-formatter \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        """,
                                Pattern.MULTILINE
                        ))
        );
    }

}
