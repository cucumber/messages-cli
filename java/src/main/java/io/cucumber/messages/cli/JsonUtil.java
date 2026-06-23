package io.cucumber.messages.cli;

import io.cucumber.messages.NdjsonToMessageReader;
import io.cucumber.messages.ndjson.Deserializer;
import io.cucumber.messages.ndjson.Json;
import io.cucumber.messages.ndjson.Serializer;
import io.cucumber.messages.types.Envelope;

final class JsonUtil {

    private static final Json instance = Json.instance().orElseThrow();

    private JsonUtil(){
        /* no-op */
    }

    static <T> Deserializer<T> deserializer(Class<T> type) {
        return instance.deserializer(type);
    }

    static <T> Serializer<T> serializer(Class<T> type) {
        return instance.serializer(type);
    }

    static NdjsonToMessageReader.Deserializer deserializer() {
        var deserializer = deserializer(Envelope.class);
        return deserializer::readValue;
    }
}
