package com.example.demo.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long senderId;
    private String senderEmail;
    
    private String receiverEmail;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private LocalDateTime sentAt;

    private boolean isRead;
    private boolean isDraft;

    private Long parentEmailId;

    public EmailMessage() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
        this.isDraft = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    public Long getParentEmailId() {
        return parentEmailId;
    }

    public void setParentEmailId(Long parentEmailId) {
        this.parentEmailId = parentEmailId;
    }
}
