package io.condense.summarize;

import static org.assertj.core.api.Assertions.assertThat;

import io.condense.ingest.edgar.DocumentTextExtractor;
import org.junit.jupiter.api.Test;

class DocumentTextExtractorTest {

    private final DocumentTextExtractor extractor = new DocumentTextExtractor();

    @Test
    void stripsTagsAndScripts() {
        String html = "<html><head><style>body{color:red}</style></head>"
                + "<body><script>alert(1)</script><p>Net income was &amp;#160;up.</p></body></html>";

        String text = extractor.extract(html, 1000);

        assertThat(text).doesNotContain("<p>").doesNotContain("alert");
        assertThat(text).contains("Net income was");
    }

    @Test
    void decodesCommonEntities() {
        assertThat(extractor.extract("<p>Q&amp;A &quot;guidance&quot;</p>", 1000))
                .contains("Q&A")
                .contains("\"guidance\"");
    }

    @Test
    void truncatesToMaxChars() {
        String html = "<p>" + "a".repeat(500) + "</p>";

        assertThat(extractor.extract(html, 100)).hasSize(100);
    }

    @Test
    void returnsEmptyForNullInput() {
        assertThat(extractor.extract(null, 100)).isEmpty();
    }
}
