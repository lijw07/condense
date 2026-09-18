package io.condense.auth;

import io.condense.config.DigestProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MagicLinkService magicLinks;
    private final SubscriberSession subscriberSession;
    private final DigestProperties digestProperties;

    public AuthController(MagicLinkService magicLinks,
                          SubscriberSession subscriberSession,
                          DigestProperties digestProperties) {
        this.magicLinks = magicLinks;
        this.subscriberSession = subscriberSession;
        this.digestProperties = digestProperties;
    }

    @PostMapping("/request-link")
    public ResponseEntity<Void> requestLink(@Valid @RequestBody LinkRequest request) {
        magicLinks.requestLink(request.email());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token, HttpServletRequest request) {
        subscriberSession.start(request, magicLinks.verify(token));
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(digestProperties.siteUrl() + "/dashboard"))
                .build();
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        subscriberSession.end(request);
        return ResponseEntity.noContent().build();
    }
}
