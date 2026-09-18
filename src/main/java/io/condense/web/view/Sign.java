package io.condense.web.view;

public enum Sign {
    UP("#22c55e"),
    DOWN("#f43f5e"),
    FLAT("#8f8f96");

    private final String emailColor;

    Sign(String emailColor) {
        this.emailColor = emailColor;
    }

    public static Sign of(String delta) {
        if (delta == null || delta.isBlank()) {
            return FLAT;
        }
        char first = delta.trim().charAt(0);
        if (first == '+') {
            return UP;
        }
        if (first == '-' || first == '−') {
            return DOWN;
        }
        return FLAT;
    }

    public String css() {
        return name().toLowerCase();
    }

    public String emailColor() {
        return emailColor;
    }
}
