package io.condense.digest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "delivery_log")
public class DeliveryLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "subscriber_id", nullable = false)
    private UUID subscriberId;

    @Column(name = "digest_date", nullable = false)
    private LocalDate digestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryStatus status;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected DeliveryLog() {
    }

    private DeliveryLog(UUID subscriberId, LocalDate digestDate, DeliveryStatus status) {
        this.subscriberId = subscriberId;
        this.digestDate = digestDate;
        this.status = status;
    }

    public static DeliveryLog sent(UUID subscriberId, LocalDate digestDate, String providerMessageId) {
        DeliveryLog entry = new DeliveryLog(subscriberId, digestDate, DeliveryStatus.SENT);
        entry.providerMessageId = providerMessageId;
        return entry;
    }

    public static DeliveryLog skipped(UUID subscriberId, LocalDate digestDate) {
        return new DeliveryLog(subscriberId, digestDate, DeliveryStatus.SKIPPED_EMPTY);
    }

    public static DeliveryLog failed(UUID subscriberId, LocalDate digestDate, String failureReason) {
        DeliveryLog entry = new DeliveryLog(subscriberId, digestDate, DeliveryStatus.FAILED);
        entry.failureReason = failureReason;
        return entry;
    }

    public DeliveryStatus getStatus() {
        return status;
    }
}
