package io.condense.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final SubscriberSession subscriberSession;

    public SessionAuthInterceptor(SubscriberSession subscriberSession) {
        this.subscriberSession = subscriberSession;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (subscriberSession.currentSubscriberId(request).isPresent()) {
            return true;
        }
        throw new UnauthenticatedException("Sign in with the link sent to your email");
    }
}
