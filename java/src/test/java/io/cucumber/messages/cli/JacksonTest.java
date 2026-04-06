package io.cucumber.messages.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.cucumber.messages.cli.Jackson.OBJECT_MAPPER;
import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;

class JacksonTest {
    
    @Test
    void can_deserialize_enum() throws JsonProcessingException {
        Source source = new Source("hello.feature", "Feature: Hello", TEXT_X_CUCUMBER_GHERKIN_PLAIN);
        String json = OBJECT_MAPPER.writeValueAsString(source);
        assertThat(OBJECT_MAPPER.readValue(json, Source.class)).isEqualTo(source);
    }

    @Test
    void serialize_enums_using_value() throws JsonProcessingException {
        assertThat(OBJECT_MAPPER.writeValueAsString(TEXT_X_CUCUMBER_GHERKIN_PLAIN))
                .isEqualTo("\"text/x.cucumber.gherkin+plain\"");
    }

    @Test
    void can_deserialize_envelope() throws JsonProcessingException {
        Envelope source = Envelope.of(new TestRunStarted(new Timestamp(3L, 14), UUID.randomUUID().toString()));
        String json = OBJECT_MAPPER.writeValueAsString(source);
        assertThat(OBJECT_MAPPER.readValue(json, Envelope.class)).isEqualTo(source);
    }
}
