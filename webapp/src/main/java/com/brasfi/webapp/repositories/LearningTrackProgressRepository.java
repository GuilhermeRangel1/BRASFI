package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.LearningTrack;
import com.brasfi.webapp.entities.LearningTrackProgress;
import com.brasfi.webapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningTrackProgressRepository extends JpaRepository<LearningTrackProgress, Long> {
    Optional<LearningTrackProgress> findByTrackAndUser(LearningTrack track, User user);

    List<LearningTrackProgress> findByUser(User user);
}
