module io.cucumber.messages.cli {
    requires com.fasterxml.jackson.annotation;
    requires info.picocli;
    requires io.cucumber.htmlformatter;
    requires io.cucumber.jsonformatter;
    requires io.cucumber.junitxmlformatter;
    requires io.cucumber.messages;
    requires io.cucumber.query;
    requires io.cucumber.testngxmlformatter;
    requires org.jspecify;
    requires io.cucumber.messages.ndjson;
    requires tools.jackson.databind;

    opens io.cucumber.messages.cli to info.picocli;

}