package io.condense.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SubscriberSession {

    private static final String SUBSCRIBER_ID = "condense.subscriberId";

    public void start(HttpServletRequest request, UUID subscriberId) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        request.getSession(true).setAttribute(SUBSCRIBER_ID, subscriberId);
    }

    public Optional<UUID> currentSubscriberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((UUID) session.getAttribute(SUBSCRIBER_ID));
    }

    public void end(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
