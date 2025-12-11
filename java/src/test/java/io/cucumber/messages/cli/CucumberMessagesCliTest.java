package io.cucumber.messages.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CucumberMessagesCliTest {

    final StringWriter out = new StringWriter();
    final StringWriter err = new StringWriter();
    final CommandLine cmd = CucumberMessagesCli.createCommandLine();


    @BeforeEach
    void setup() {
        cmd.setErr(new PrintWriter(err));
        cmd.setOut(new PrintWriter(out));
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
                        .hasLineCount(5)
                        .matches(Pattern.compile("""
                                        cucumber-json-formatter \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        html-formatter \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        junit-xml-formatter \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        messages-cli \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        testng-xml-formatter \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?
                                        """,
                                Pattern.MULTILINE
                        ))
        );
    }

}
