package com.brasfi.webapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class LearningTrackStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 900)
    private String description;

    @Column(nullable = false)
    private String action;

    @ManyToOne(optional = false)
    private LearningTrack track;

    public LearningTrackStep() {
    }

    public LearningTrackStep(String title, String description, String action) {
        this.title = title;
        this.description = description;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LearningTrack getTrack() {
        return track;
    }

    public void setTrack(LearningTrack track) {
        this.track = track;
    }
}
