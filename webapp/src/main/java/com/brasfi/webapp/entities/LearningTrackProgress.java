package com.brasfi.webapp.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"track_id", "user_id"}))
public class LearningTrackProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private LearningTrack track;

    @ManyToOne(optional = false)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "learning_track_completed_steps", joinColumns = @JoinColumn(name = "progress_id"))
    @OrderColumn(name = "completed_order")
    @Column(name = "step_index", nullable = false)
    private List<Integer> completedSteps = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    public LearningTrackProgress() {
    }

    public LearningTrackProgress(LearningTrack track, User user) {
        this.track = track;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public LearningTrack getTrack() {
        return track;
    }

    public void setTrack(LearningTrack track) {
        this.track = track;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Integer> getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(List<Integer> completedSteps) {
        this.completedSteps = completedSteps == null ? new ArrayList<>() : new ArrayList<>(completedSteps);
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
