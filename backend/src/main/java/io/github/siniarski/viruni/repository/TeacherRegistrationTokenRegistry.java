package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.TeacherRegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRegistrationTokenRegistry extends JpaRepository<TeacherRegistrationToken, Long> {
    Optional<TeacherRegistrationToken> findByToken(String token);
}

