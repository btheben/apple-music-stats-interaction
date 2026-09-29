package com.example.controller;

import com.example.depot.ExclustionArtistDepot;
import com.example.modele.ArtistNormalizationRuleDto;
import com.example.service.ExclusionArtistNormalizationBackfillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/import/admin/normalize")
public class NormalizationAdminController {

    private final ExclusionArtistNormalizationBackfillService exclusionBackfillService;
    private final ExclustionArtistDepot exclustionArtistDepot;

    public NormalizationAdminController(
            ExclusionArtistNormalizationBackfillService exclusionBackfillService,
            ExclustionArtistDepot exclustionArtistDepot
    ) {
        this.exclusionBackfillService = exclusionBackfillService;
        this.exclustionArtistDepot = exclustionArtistDepot;
    }

    @PostMapping("/exclusions")
    public ResponseEntity<String> backfillExclusions() {
        int updated = exclusionBackfillService.backfillArtistNormalization();
        return ResponseEntity.ok("Exclusions mises à jour : " + updated);
    }

    @GetMapping("/exclusions/status")
    public ResponseEntity<String> exclusionBackfillStatus() {
        long remaining = exclustionArtistDepot.countByArtistNameNormalizedIsNullAndArtistNameIsNotNull();
        return ResponseEntity.ok("Exclusions restantes à normaliser : " + remaining);
    }


}