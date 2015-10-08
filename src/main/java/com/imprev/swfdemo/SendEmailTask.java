package com.imprev.swfdemo;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class SendEmailTask {

    private String recipEmail;
    private String senderEmail;
    private String subject;
    private String body;

    public SendEmailTask(String recipEmail, String senderEmail, String subject, String body) {
        this.recipEmail = recipEmail;
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.body = body;
    }

    public SendEmailTask() {
    }

    public String getRecipEmail() {
        return recipEmail;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
