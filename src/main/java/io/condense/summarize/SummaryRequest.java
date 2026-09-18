package io.condense.summarize;

public record SummaryRequest(String ticker, String companyName, String formType, String filedOn, String documentText) {
}
