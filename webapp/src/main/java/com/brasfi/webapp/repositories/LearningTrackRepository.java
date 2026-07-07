package com.brasfi.webapp.repositories;

import com.brasfi.webapp.entities.LearningTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningTrackRepository extends JpaRepository<LearningTrack, Long> {
    Optional<LearningTrack> findBySlug(String slug);

    List<LearningTrack> findAllByOrderByIdAsc();
}
