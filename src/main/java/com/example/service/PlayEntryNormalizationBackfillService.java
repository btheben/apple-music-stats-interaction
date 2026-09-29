package com.example.service;

import com.example.modele.AppleMusicEntry;
import com.example.depot.PlayEntryDepot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayEntryNormalizationBackfillService {

    private final PlayEntryDepot playEntryDepot;
    private final NormalizationService normalizationService;

    public PlayEntryNormalizationBackfillService(
            PlayEntryDepot playEntryDepot,
            NormalizationService normalizationService
    ) {
        this.playEntryDepot = playEntryDepot;
        this.normalizationService = normalizationService;
    }

    @Transactional
    public int backfillArtistNormalization() {
        int totalUpdated = 0;
        int size = 500;

        while (true) {
            Page<AppleMusicEntry> result =
                    playEntryDepot.findByArtistNameNormalizedIsNullAndArtistNameIsNotNull(
                            PageRequest.of(0, size)
                    );

            if (result.isEmpty()) {
                break;
            }

            int updatedThisBatch = 0;

            for (AppleMusicEntry entry : result.getContent()) {
                String normalized = normalizationService.normalizeArtistName(entry.getArtistName());

                if (normalized != null && !normalized.equals(entry.getArtistNameNormalized())) {
                    entry.setArtistNameNormalized(normalized);
                    updatedThisBatch++;
                    totalUpdated++;
                }
            }

            if (updatedThisBatch == 0) {
                break;
            }

            playEntryDepot.saveAll(result.getContent());
            playEntryDepot.flush();
        }

        return totalUpdated;
    }
}
