package io.condense.summarize;

import io.condense.config.OllamaProperties;
import io.condense.filing.Significance;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(name = "condense.summarizer", havingValue = "ollama", matchIfMissing = true)
public class OllamaSummarizer implements Summarizer {

    private static final int MAX_DOCUMENT_CHARS = 24_000;

    private final RestClient restClient;
    private final OllamaProperties properties;
    private final PromptLibrary prompts;
    private final JsonMapper jsonMapper;

    public OllamaSummarizer(RestClient ollamaRestClient,
                            OllamaProperties properties,
                            PromptLibrary prompts,
                            JsonMapper jsonMapper) {
        this.restClient = ollamaRestClient;
        this.properties = properties;
        this.prompts = prompts;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SummaryResult summarize(SummaryRequest request) {
        String excerpt = truncate(request.documentText());
        OllamaGenerateRequest payload = new OllamaGenerateRequest(
                properties.model(),
                prompts.filingSummary(request, excerpt),
                false,
                "json",
                Map.of("temperature", properties.temperature(), "num_ctx", properties.contextWindowTokens()));

        OllamaGenerateResponse response = restClient.post()
                .uri("/api/generate")
                .body(payload)
                .retrieve()
                .body(OllamaGenerateResponse.class);

        if (response == null || response.response() == null || response.response().isBlank()) {
            throw new SummarizationException("Ollama returned an empty response for " + request.formType());
        }
        return toResult(response);
    }

    private SummaryResult toResult(OllamaGenerateResponse response) {
        SummaryPayload payload = jsonMapper.readValue(response.response(), SummaryPayload.class);
        return new SummaryResult(
                response.model(),
                payload.headline(),
                payload.bullets() == null ? List.of() : List.copyOf(payload.bullets()),
                parseSignificance(payload.significance()));
    }

    private Significance parseSignificance(String value) {
        if (value == null) {
            return Significance.ROUTINE;
        }
        try {
            return Significance.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Significance.ROUTINE;
        }
    }

    private String truncate(String documentText) {
        if (documentText == null) {
            return "";
        }
        return documentText.length() <= MAX_DOCUMENT_CHARS
                ? documentText
                : documentText.substring(0, MAX_DOCUMENT_CHARS);
    }
}
