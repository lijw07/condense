package io.condense.email;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailRenderer {

    private final TemplateEngine templateEngine;

    public EmailRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(String template, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process(template, context);
    }
}
