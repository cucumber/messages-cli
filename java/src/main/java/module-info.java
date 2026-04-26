module io.cucumber.messages.cli {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires info.picocli;
    requires io.cucumber.htmlformatter;
    requires io.cucumber.jsonformatter;
    requires io.cucumber.junitxmlformatter;
    requires io.cucumber.messages;
    requires io.cucumber.query;
    requires io.cucumber.testngxmlformatter;
    requires org.jspecify;

    opens io.cucumber.messages.cli to info.picocli;

}