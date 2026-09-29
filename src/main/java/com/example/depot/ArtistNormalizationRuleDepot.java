package com.example.depot;

import com.example.modele.ArtistNormalizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistNormalizationRuleDepot extends JpaRepository<ArtistNormalizationRule, Long> {

    ArtistNormalizationRule findFirstByRawArtistNameNormalized(String rawArtistNameNormalized);

    ArtistNormalizationRule findFirstByCanonicalArtistNameNormalized(String canonicalArtistNameNormalized);
}