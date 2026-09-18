package io.condense.subscriber;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription")
public class Subscription {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @Column(nullable = false, length = 10)
    private String cik;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Subscription() {
    }

    public Subscription(Subscriber subscriber, String cik) {
        this.subscriber = subscriber;
        this.cik = cik;
    }

    public UUID getId() {
        return id;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public String getCik() {
        return cik;
    }
}
