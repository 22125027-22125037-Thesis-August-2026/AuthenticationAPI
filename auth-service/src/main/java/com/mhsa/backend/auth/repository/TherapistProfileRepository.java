package com.mhsa.backend.auth.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mhsa.backend.auth.model.TherapistProfile;

/**
 * Repository scoped to {@link TherapistProfile} (the JOINED-inheritance subtype). Spring Data
 * restricts queries to therapist rows, so {@code findAll(Pageable)} returns only therapists —
 * exactly the set therapist-api reconciles against.
 */
@Repository
public interface TherapistProfileRepository extends JpaRepository<TherapistProfile, UUID> {

    @Override
    Page<TherapistProfile> findAll(Pageable pageable);
}
