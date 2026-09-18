package io.condense.ingest.edgar;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DocumentTextExtractor {

    private static final Pattern SCRIPT_OR_STYLE =
            Pattern.compile("(?is)<(script|style)[^>]*>.*?</\\1>");
    private static final Pattern TAG = Pattern.compile("(?s)<[^>]+>");
    private static final Pattern WHITESPACE = Pattern.compile("[ \\t\\x0B\\f\\r]+");
    private static final Pattern BLANK_LINES = Pattern.compile("\\n{3,}");

    public String extract(String html, int maxChars) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String withoutScripts = SCRIPT_OR_STYLE.matcher(html).replaceAll(" ");
        String withoutTags = TAG.matcher(withoutScripts).replaceAll(" ");
        String decoded = decodeEntities(withoutTags);
        String collapsed = WHITESPACE.matcher(decoded).replaceAll(" ");
        String trimmed = BLANK_LINES.matcher(collapsed).replaceAll("\n\n").trim();
        return trimmed.length() <= maxChars ? trimmed : trimmed.substring(0, maxChars);
    }

    private String decodeEntities(String value) {
        return value
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }
}
