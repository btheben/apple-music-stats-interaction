package com.example.service;

import com.example.depot.ExclustionArtistDepot;
import com.example.modele.ExclustionArtist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExclusionArtistNormalizationBackfillService {

    private final ExclustionArtistDepot exclustionArtistDepot;
    private final NormalizationService normalizationService;

    public ExclusionArtistNormalizationBackfillService(
            ExclustionArtistDepot exclustionArtistDepot,
            NormalizationService normalizationService
    ) {
        this.exclustionArtistDepot = exclustionArtistDepot;
        this.normalizationService = normalizationService;
    }

    @Transactional
    public int backfillArtistNormalization() {
        int totalUpdated = 0;

        List<ExclustionArtist> exclusions = exclustionArtistDepot.findAll();

        for (ExclustionArtist exclusion : exclusions) {
            String normalized = normalizationService.normalizeArtistName(exclusion.getArtistName());

            if (normalized != null && !normalized.equals(exclusion.getArtistNameNormalized())) {
                exclusion.setArtistNameNormalized(normalized);
                totalUpdated++;
            }
        }

        exclustionArtistDepot.saveAll(exclusions);
        exclustionArtistDepot.flush();

        return totalUpdated;
    }
}