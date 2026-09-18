package io.condense.email;

public interface EmailSender {

    String send(EmailMessage message);
}
