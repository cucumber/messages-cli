package io.cucumber.messages.cli;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.cucumber.messages.NdjsonToMessageIterable;
import io.cucumber.messages.types.Envelope;

class Jackson {
    static NdjsonToMessageIterable.Deserializer deserializer() {
        var jsonMapper = JsonMapper.builder()
                .addModule(new Jdk8Module())
                .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .enable(DeserializationFeature.USE_LONG_FOR_INTS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        return json -> jsonMapper.readValue(json, Envelope.class);
    }
}
