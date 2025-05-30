package com.brasfi.webapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Post {

    @Id
    private Long id;

    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
