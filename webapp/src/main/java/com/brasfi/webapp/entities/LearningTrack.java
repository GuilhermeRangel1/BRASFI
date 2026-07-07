package com.brasfi.webapp.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

@Entity
public class LearningTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false, length = 1200)
    private String description;

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "step_order")
    private List<LearningTrackStep> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "learning_track_outcomes", joinColumns = @JoinColumn(name = "track_id"))
    @OrderColumn(name = "outcome_order")
    @Column(name = "outcome", nullable = false, length = 500)
    private List<String> outcomes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "learning_track_resources", joinColumns = @JoinColumn(name = "track_id"))
    @OrderColumn(name = "resource_order")
    @Column(name = "resource", nullable = false, length = 500)
    private List<String> resources = new ArrayList<>();

    public LearningTrack() {
    }

    public LearningTrack(String slug, String title, String level, String duration, String description) {
        this.slug = slug;
        this.title = title;
        this.level = level;
        this.duration = duration;
        this.description = description;
    }

    public void addStep(LearningTrackStep step) {
        steps.add(step);
        step.setTrack(this);
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LearningTrackStep> getSteps() {
        return steps;
    }

    public void setSteps(List<LearningTrackStep> steps) {
        this.steps.clear();
        if (steps != null) {
            steps.forEach(this::addStep);
        }
    }

    public List<String> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes == null ? new ArrayList<>() : new ArrayList<>(outcomes);
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources == null ? new ArrayList<>() : new ArrayList<>(resources);
    }
}
