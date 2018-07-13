package ru.ssnd.demo.vkchat.entity;

import javax.persistence.Entity;

@Entity
public class Sender {

    private Long id;
    private String name;

    public Sender(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
