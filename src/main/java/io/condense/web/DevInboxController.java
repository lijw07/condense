package io.condense.web;

import io.condense.email.DevInbox;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dev/inbox")
@ConditionalOnProperty(name = "condense.email.provider", havingValue = "dev-inbox")
public class DevInboxController {

    private final DevInbox inbox;

    public DevInboxController(DevInbox inbox) {
        this.inbox = inbox;
    }

    @GetMapping
    public String inbox(Model model) {
        model.addAttribute("emails", inbox.recent());
        return "dev-inbox";
    }

    @PostMapping("/clear")
    public String clear() {
        inbox.clear();
        return "redirect:/dev/inbox";
    }
}
