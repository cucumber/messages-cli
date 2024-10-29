package io.cucumber.messages.cli;

import picocli.CommandLine.IVersionProvider;

import java.io.IOException;
import java.util.Properties;

class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        try {
            var properties = loadVersions();
            return properties.stringPropertyNames()
                    .stream()
                    .sorted()
                    .map(artifact -> artifact + " " + properties.getProperty(artifact))
                    .toArray(String[]::new);
        } catch (IOException e) {
            return new String[0];
        }
    }

    private static Properties loadVersions() throws IOException {
        var properties = new Properties();
        properties.load(VersionProvider.class.getResourceAsStream("versions.properties"));
        return properties;
    }

}
