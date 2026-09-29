package com.example.depot;

import com.example.modele.ExclustionArtist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExclustionArtistDepot extends JpaRepository<ExclustionArtist, Long> {

    Page<ExclustionArtist> findByArtistNameNormalizedIsNullAndArtistNameIsNotNull(Pageable pageable);

    long countByArtistNameNormalizedIsNull();

    long countByArtistNameNormalizedIsNullAndArtistNameIsNotNull();

    boolean existsByArtistNameNormalized(String artistNameNormalized);
}