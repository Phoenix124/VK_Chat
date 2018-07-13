package ru.ssnd.demo.vkchat.entity;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class Message {

    private Long id;
    private String text;
    private Date date;
    private Sender sender;

    public Message() {
    }

    public Message(Long id, String text, Date sentAt, Sender sender) {
        this.id = id;
        this.text = text;
        this.date = sentAt;
        this.sender = sender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}