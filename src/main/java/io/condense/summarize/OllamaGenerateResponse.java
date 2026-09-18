package io.condense.summarize;

public record OllamaGenerateResponse(String model, String response, boolean done) {
}
