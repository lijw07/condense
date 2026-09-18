package io.condense.email;

public record EmailMessage(String to, String subject, String htmlBody, String textBody, String listUnsubscribeUrl) {
}
