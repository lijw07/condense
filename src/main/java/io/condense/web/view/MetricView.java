package io.condense.web.view;

public record MetricView(String label, String value, String qoq, String yoy) {

    public Sign qoqSign() {
        return Sign.of(qoq);
    }

    public Sign yoySign() {
        return Sign.of(yoy);
    }
}
