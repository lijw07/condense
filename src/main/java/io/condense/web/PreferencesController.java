package io.condense.web;

import io.condense.auth.SubscriberSession;
import io.condense.auth.UnauthenticatedException;
import io.condense.web.data.SubscriberPreferences;
import io.condense.web.view.PreferencesUpdate;
import io.condense.web.view.PreferencesView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/preferences")
public class PreferencesController {

    private final SubscriberSession subscriberSession;
    private final SubscriberPreferences preferences;

    public PreferencesController(SubscriberSession subscriberSession, SubscriberPreferences preferences) {
        this.subscriberSession = subscriberSession;
        this.preferences = preferences;
    }

    @PostMapping
    public PreferencesView update(@RequestBody PreferencesUpdate update, HttpServletRequest request) {
        return preferences.update(currentSubscriberId(request), update);
    }

    private UUID currentSubscriberId(HttpServletRequest request) {
        return subscriberSession.currentSubscriberId(request)
                .orElseThrow(() -> new UnauthenticatedException("Sign in with the link sent to your email"));
    }
}
