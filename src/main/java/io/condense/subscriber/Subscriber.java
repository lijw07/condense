package io.condense.subscriber;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "subscriber")
public class Subscriber {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SubscriberStatus status = SubscriberStatus.PENDING_CONFIRMATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Cadence cadence = Cadence.DAILY;

    @Column(name = "unsubscribe_token", nullable = false, unique = true, length = 64)
    private String unsubscribeToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_style", nullable = false, length = 32)
    private SummaryStyle summaryStyle = SummaryStyle.KEY_POINTS_AND_NUMBERS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Tone tone = Tone.NEUTRAL_ANALYST;

    @Column(name = "calendar_reminders", nullable = false)
    private boolean calendarReminders = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Appearance appearance = Appearance.DARK;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subscription> subscriptions = new LinkedHashSet<>();

    protected Subscriber() {
    }

    public Subscriber(String email, String unsubscribeToken) {
        this.email = email;
        this.unsubscribeToken = unsubscribeToken;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public SubscriberStatus getStatus() {
        return status;
    }

    public Cadence getCadence() {
        return cadence;
    }

    public SummaryStyle getSummaryStyle() {
        return summaryStyle;
    }

    public Tone getTone() {
        return tone;
    }

    public boolean isCalendarReminders() {
        return calendarReminders;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public String getUnsubscribeToken() {
        return unsubscribeToken;
    }

    public Set<Subscription> getSubscriptions() {
        return Set.copyOf(subscriptions);
    }

    public boolean isActive() {
        return status == SubscriberStatus.ACTIVE;
    }

    public void confirm() {
        this.status = SubscriberStatus.ACTIVE;
        this.confirmedAt = Instant.now();
    }

    public void unsubscribe() {
        this.status = SubscriberStatus.UNSUBSCRIBED;
        this.subscriptions.clear();
    }

    public void changeCadence(Cadence cadence) {
        this.cadence = cadence;
    }

    public void restyleTo(SummaryStyle summaryStyle) {
        this.summaryStyle = summaryStyle;
    }

    public void retoneTo(Tone tone) {
        this.tone = tone;
    }

    public void calendarRemindersEnabled(boolean enabled) {
        this.calendarReminders = enabled;
    }

    public void appearAs(Appearance appearance) {
        this.appearance = appearance;
    }

    public void follow(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void unfollow(String cik) {
        subscriptions.removeIf(subscription -> subscription.getCik().equals(cik));
    }

    public boolean follows(String cik) {
        return subscriptions.stream().anyMatch(subscription -> subscription.getCik().equals(cik));
    }
}
