package io.condense.summarize;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptLibrary {

    private final String filingSummaryTemplate = load("prompts/filing-summary.txt");

    public String filingSummary(SummaryRequest request, String documentExcerpt) {
        return filingSummaryTemplate
                .replace("{{ticker}}", nullSafe(request.ticker()))
                .replace("{{company}}", nullSafe(request.companyName()))
                .replace("{{form}}", nullSafe(request.formType()))
                .replace("{{filedOn}}", nullSafe(request.filedOn()))
                .replace("{{document}}", nullSafe(documentExcerpt));
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String load(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load prompt " + path, e);
        }
    }
}
