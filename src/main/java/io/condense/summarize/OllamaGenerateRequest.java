package io.condense.summarize;

import java.util.Map;

public record OllamaGenerateRequest(String model, String prompt, boolean stream, String format, Map<String, Object> options) {
}
