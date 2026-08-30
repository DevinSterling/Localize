package com.devinsterling.localize.icu4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class IcuProperties {
    private static final String PROPERTIES_FILE = "localize.properties";
    private static final String FORMATTER_PROPERTY = "localize.icu4j.formatter";
    private static final String DEFAULT_FORMATTER = "com.ibm.icu.message2.MessageFormatter";

    public static final IcuProperties INSTANCE = new IcuProperties(PROPERTIES_FILE);

    private final Properties properties;

    // package-private to allow tests
    IcuProperties(String propertiesFile) {
        this.properties = loadProperties(propertiesFile);
    }

    private Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream input = loader.getResourceAsStream(propertiesFile)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + propertiesFile, e);
        }

        return properties;
    }

    private String get(String key, String defaultValue) {
        String value = System.getProperty(key, properties.getProperty(key));
        return value == null ? defaultValue : value;
    }

    public IcuMessageFormat getMessageFormat() {
        return IcuMessageFormat.from(get(FORMATTER_PROPERTY, DEFAULT_FORMATTER));
    }
}