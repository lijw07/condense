package io.condense.web.view;

public record PreferencesView(String summaryStyle,
                              String delivery,
                              String tone,
                              boolean calendarReminders,
                              String appearance) {

    public boolean summaryStyleIs(String candidate) {
        return candidate.equals(summaryStyle);
    }

    public boolean deliveryIs(String candidate) {
        return candidate.equals(delivery);
    }

    public boolean toneIs(String candidate) {
        return candidate.equals(tone);
    }

    public boolean appearanceIs(String candidate) {
        return candidate.equals(appearance);
    }

    public String themeAttribute() {
        return appearance.toLowerCase();
    }
}
