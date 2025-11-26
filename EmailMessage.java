package com.healthtrack360.email;

import java.util.List;

public class EmailMessage {

    private String to;
    private List<String> cc;
    private String subject;
    private String body;

    public EmailMessage() {
    }

    public EmailMessage(String to, List<String> cc, String subject, String body) {
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
