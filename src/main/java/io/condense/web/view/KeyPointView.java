package io.condense.web.view;

public record KeyPointView(int ordinal, String text, String delta) {

    public String number() {
        return String.format("%02d", ordinal);
    }

    public Sign sign() {
        return Sign.of(delta);
    }
}
