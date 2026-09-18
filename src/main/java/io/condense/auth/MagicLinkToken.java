package io.condense.auth;

import io.condense.subscriber.Subscriber;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "magic_link_token")
public class MagicLinkToken {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MagicLinkPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected MagicLinkToken() {
    }

    public MagicLinkToken(Subscriber subscriber, String tokenHash, MagicLinkPurpose purpose, Instant expiresAt) {
        this.subscriber = subscriber;
        this.tokenHash = tokenHash;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public MagicLinkPurpose getPurpose() {
        return purpose;
    }

    public boolean isUsable(Instant now) {
        return consumedAt == null && now.isBefore(expiresAt);
    }

    public void consume() {
        this.consumedAt = Instant.now();
    }
}
