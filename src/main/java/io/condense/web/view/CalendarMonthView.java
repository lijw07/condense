package io.condense.web.view;

import java.util.List;

public record CalendarMonthView(String month, List<CalendarEventView> events) {
}
