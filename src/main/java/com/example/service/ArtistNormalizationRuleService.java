package com.example.service;

import com.example.depot.ArtistNormalizationRuleDepot;
import com.example.depot.PlayEntryDepot;
import com.example.modele.ArtistNormalizationRule;
import com.example.modele.ArtistNormalizationRuleDto;
import com.example.modele.CanonicalArtistResolution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtistNormalizationRuleService {

    private final ArtistNormalizationRuleDepot artistNormalizationRuleDepot;
    private final PlayEntryDepot playEntryDepot;
    private final NormalizationService normalizationService;

    public ArtistNormalizationRuleService(ArtistNormalizationRuleDepot artistNormalizationRuleDepot,
                                          PlayEntryDepot playEntryDepot,
                                          NormalizationService normalizationService) {
        this.artistNormalizationRuleDepot = artistNormalizationRuleDepot;
        this.playEntryDepot = playEntryDepot;
        this.normalizationService = normalizationService;
    }

    @Transactional
    public int createOrUpdateRule(String rawArtistName, String canonicalArtistName) {
        if (rawArtistName == null || rawArtistName.trim().isEmpty()) {
            throw new IllegalArgumentException("rawArtistName ne peut pas être vide");
        }

        if (canonicalArtistName == null || canonicalArtistName.trim().isEmpty()) {
            throw new IllegalArgumentException("canonicalArtistName ne peut pas être vide");
        }

        String safeRawArtistName = rawArtistName.trim();
        String safeCanonicalArtistName = canonicalArtistName.trim();

        String rawNormalized = normalizationService.normalizeArtistName(safeRawArtistName);
        String canonicalNormalized = normalizationService.normalizeArtistName(safeCanonicalArtistName);

        if (rawNormalized == null || rawNormalized.isBlank()) {
            throw new IllegalArgumentException("Impossible de normaliser rawArtistName");
        }

        if (canonicalNormalized == null || canonicalNormalized.isBlank()) {
            throw new IllegalArgumentException("Impossible de normaliser canonicalArtistName");
        }

        ArtistNormalizationRule rule =
                artistNormalizationRuleDepot.findFirstByRawArtistNameNormalized(rawNormalized);

        if (rule == null) {
            rule = new ArtistNormalizationRule();
        }

        rule.setRawArtistName(safeRawArtistName);
        rule.setRawArtistNameNormalized(rawNormalized);
        rule.setCanonicalArtistName(safeCanonicalArtistName);
        rule.setCanonicalArtistNameNormalized(canonicalNormalized);

        artistNormalizationRuleDepot.save(rule);

        return playEntryDepot.updateArtistNormalizedName(rawNormalized, canonicalNormalized);
    }

    public String resolveCanonicalArtistNormalized(String rawArtistName) {
        CanonicalArtistResolution resolution = resolveCanonicalArtist(rawArtistName);
        return resolution.getCanonicalArtistNameNormalized();
    }

    public String resolveCanonicalArtistDisplayName(String rawArtistName) {
        CanonicalArtistResolution resolution = resolveCanonicalArtist(rawArtistName);
        return resolution.getCanonicalArtistName();
    }

    public CanonicalArtistResolution resolveCanonicalArtist(String rawArtistName) {
        String safeRawArtistName = rawArtistName == null ? "" : rawArtistName.trim();
        String rawNormalized = normalizationService.normalizeArtistName(safeRawArtistName);

        CanonicalArtistResolution resolution = new CanonicalArtistResolution();
        resolution.setRequestedArtistName(safeRawArtistName);
        resolution.setRequestedArtistNameNormalized(rawNormalized);

        if (rawNormalized == null || rawNormalized.isBlank()) {
            resolution.setCanonicalArtistName(safeRawArtistName);
            resolution.setCanonicalArtistNameNormalized(rawNormalized);
            return resolution;
        }

        ArtistNormalizationRule directRule =
                artistNormalizationRuleDepot.findFirstByRawArtistNameNormalized(rawNormalized);

        if (directRule != null) {
            resolution.setCanonicalArtistName(
                    safeValue(directRule.getCanonicalArtistName(), safeRawArtistName)
            );
            resolution.setCanonicalArtistNameNormalized(
                    safeValue(directRule.getCanonicalArtistNameNormalized(), rawNormalized)
            );
            return resolution;
        }

        ArtistNormalizationRule canonicalRule =
                artistNormalizationRuleDepot.findFirstByCanonicalArtistNameNormalized(rawNormalized);

        if (canonicalRule != null) {
            resolution.setCanonicalArtistName(
                    safeValue(canonicalRule.getCanonicalArtistName(), safeRawArtistName)
            );
            resolution.setCanonicalArtistNameNormalized(
                    safeValue(canonicalRule.getCanonicalArtistNameNormalized(), rawNormalized)
            );
            return resolution;
        }

        resolution.setCanonicalArtistName(safeRawArtistName);
        resolution.setCanonicalArtistNameNormalized(rawNormalized);
        return resolution;
    }

    public ArtistNormalizationRuleDto findRuleByArtistName(String artistName) {
        String normalized = normalizationService.normalizeArtistName(artistName);

        ArtistNormalizationRuleDto dto = new ArtistNormalizationRuleDto();
        dto.setRawArtistName(artistName);
        dto.setRawArtistNameNormalized(normalized);

        if (normalized == null || normalized.isBlank()) {
            dto.setExists(false);
            return dto;
        }

        ArtistNormalizationRule rule =
                artistNormalizationRuleDepot.findFirstByRawArtistNameNormalized(normalized);

        if (rule == null) {
            rule = artistNormalizationRuleDepot.findFirstByCanonicalArtistNameNormalized(normalized);
        }

        if (rule == null) {
            dto.setExists(false);
            return dto;
        }

        dto.setExists(true);
        dto.setRawArtistName(rule.getRawArtistName());
        dto.setRawArtistNameNormalized(rule.getRawArtistNameNormalized());
        dto.setCanonicalArtistName(rule.getCanonicalArtistName());
        dto.setCanonicalArtistNameNormalized(rule.getCanonicalArtistNameNormalized());

        return dto;
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}