package io.cucumber.messages.cli;

import picocli.CommandLine.IVersionProvider;

import java.util.Optional;
import java.util.function.Function;

public class ManifestVersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        // TODO: Include versions for junit xml, testng xml, gherkin, ect.
        // Use Maven to process a file and add versions.
        var version = getAttribute(Package::getImplementationVersion).orElse("DEVELOPMENT");
        return new String[]{"messages-cli " + version};
    }

    private static Optional<String> getAttribute(Function<Package, String> function) {
        return Optional.ofNullable(ManifestVersionProvider.class.getPackage()).map(function);
    }
}
