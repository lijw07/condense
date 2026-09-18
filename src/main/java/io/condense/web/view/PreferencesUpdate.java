package io.condense.web.view;

public record PreferencesUpdate(String summaryStyle,
                                String delivery,
                                String tone,
                                Boolean calendarReminders,
                                String appearance) {
}
