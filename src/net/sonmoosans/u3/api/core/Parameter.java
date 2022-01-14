package net.sonmoosans.u3.api.core;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public record Parameter(String name, String value) {
    public static Parameter intValue(String name, int value) {
        return new Parameter(name, String.valueOf(value));
    }

    public static Parameter boolValue(String name, boolean value) {
        return new Parameter(name, String.valueOf(value));
    }

    @Override
    public String toString() {
        return URLEncoder.encode(name, StandardCharsets.UTF_8) +
                "=" +
                URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
