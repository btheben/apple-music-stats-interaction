package com.example.controller;

import com.example.modele.ArtistNormalizationRuleDto;
import com.example.modele.ArtistNormalizationRuleRequest;
import com.example.service.ArtistNormalizationRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/artist-normalization")
public class ArtistNormalizationRuleController {

    private final ArtistNormalizationRuleService artistNormalizationRuleService;

    public ArtistNormalizationRuleController(ArtistNormalizationRuleService artistNormalizationRuleService) {
        this.artistNormalizationRuleService = artistNormalizationRuleService;
    }

    @PostMapping
    public ResponseEntity<String> createOrUpdateRule(@RequestBody ArtistNormalizationRuleRequest request) {
        int updated = artistNormalizationRuleService.createOrUpdateRule(
                request.getRawArtistName(),
                request.getCanonicalArtistName()
        );

        return ResponseEntity.ok("Règle créée/mise à jour. Entrées historiques mises à jour : " + updated);
    }

    @GetMapping("/lookup")
    public ResponseEntity<ArtistNormalizationRuleDto> lookupRule(@RequestParam String artistName) {
        ArtistNormalizationRuleDto dto = artistNormalizationRuleService.findRuleByArtistName(artistName);
        return ResponseEntity.ok(dto);
    }
}
