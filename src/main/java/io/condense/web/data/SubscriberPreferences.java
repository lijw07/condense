package io.condense.web.data;

import io.condense.auth.UnauthenticatedException;
import io.condense.subscriber.Appearance;
import io.condense.subscriber.Cadence;
import io.condense.subscriber.Subscriber;
import io.condense.subscriber.SubscriberRepository;
import io.condense.subscriber.SummaryStyle;
import io.condense.subscriber.Tone;
import io.condense.web.view.PreferencesUpdate;
import io.condense.web.view.PreferencesView;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriberPreferences {

    private final SubscriberRepository subscribers;

    public SubscriberPreferences(SubscriberRepository subscribers) {
        this.subscribers = subscribers;
    }

    @Transactional(readOnly = true)
    public PreferencesView forSubscriber(UUID subscriberId) {
        return toView(require(subscriberId));
    }

    @Transactional
    public PreferencesView update(UUID subscriberId, PreferencesUpdate update) {
        Subscriber subscriber = require(subscriberId);
        if (update.summaryStyle() != null) {
            subscriber.restyleTo(SummaryStyle.valueOf(update.summaryStyle()));
        }
        if (update.delivery() != null) {
            subscriber.changeCadence(Cadence.valueOf(update.delivery()));
        }
        if (update.tone() != null) {
            subscriber.retoneTo(Tone.valueOf(update.tone()));
        }
        if (update.calendarReminders() != null) {
            subscriber.calendarRemindersEnabled(update.calendarReminders());
        }
        if (update.appearance() != null) {
            subscriber.appearAs(Appearance.valueOf(update.appearance()));
        }
        return toView(subscriber);
    }

    private PreferencesView toView(Subscriber subscriber) {
        return new PreferencesView(
                subscriber.getSummaryStyle().name(),
                subscriber.getCadence().name(),
                subscriber.getTone().name(),
                subscriber.isCalendarReminders(),
                subscriber.getAppearance().name());
    }

    private Subscriber require(UUID subscriberId) {
        return subscribers.findById(subscriberId)
                .orElseThrow(() -> new UnauthenticatedException("Session no longer valid"));
    }
}
