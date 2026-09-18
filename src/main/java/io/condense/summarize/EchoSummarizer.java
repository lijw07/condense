package io.condense.summarize;

import io.condense.filing.Significance;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "condense.summarizer", havingValue = "echo")
public class EchoSummarizer implements Summarizer {

    private static final int HEADLINE_CHARS = 140;

    @Override
    public SummaryResult summarize(SummaryRequest request) {
        String text = request.documentText() == null ? "" : request.documentText().strip();
        String headline = text.length() <= HEADLINE_CHARS ? text : text.substring(0, HEADLINE_CHARS);
        return new SummaryResult(
                "echo",
                "%s %s: %s".formatted(request.ticker(), request.formType(), headline),
                List.of("Stub summary generated without a model."),
                Significance.ROUTINE);
    }
}
