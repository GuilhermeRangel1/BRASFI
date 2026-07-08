package com.brasfi.webapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "learning_track_step_materials", joinColumns = @JoinColumn(name = "step_id"))
    @OrderColumn(name = "material_order")
    @Column(name = "material", nullable = false, length = 600)
    private List<String> materials = new ArrayList<>();

    @ManyToOne(optional = false)
    private LearningTrack track;

    public LearningTrackStep() {
    }

    public LearningTrackStep(String title, String description, String action) {
        this.title = title;
        this.description = description;
        this.action = action;
    }

    public LearningTrackStep(String title, String description, String action, List<String> materials) {
        this.title = title;
        this.description = description;
        this.action = action;
        setMaterials(materials);
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

    public List<String> getMaterials() {
        return materials;
    }

    public void setMaterials(List<String> materials) {
        this.materials = materials == null ? new ArrayList<>() : new ArrayList<>(materials);
    }

    public LearningTrack getTrack() {
        return track;
    }

    public void setTrack(LearningTrack track) {
        this.track = track;
    }
}
