package com.example.controller;

import com.example.modele.Artist;
import com.example.modele.ArtistVariantSearchProjection;
import com.example.modele.TopArtist;
import com.example.service.PlayEntryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artists")
public class ArtistController extends BaseDateController {

    private final PlayEntryService playEntryService;

    public ArtistController(PlayEntryService playEntryService) {
        this.playEntryService = playEntryService;
    }

    @GetMapping("/{artistName}")
    public Artist getArtist(@PathVariable String artistName) {
        return playEntryService.getByArtist(artistName);
    }

    @GetMapping("/top")
    public List<TopArtist> getTopArtists(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        Integer safeLimit = limitValue != null ? limitValue : 50;

        return playEntryService.getTopArtistDateInterval(
                range.getDateStart(),
                range.getDateEnd(),
                safeLimit
        );
    }

    @PostMapping("/exclusions")
    public void addExclusionArtist(@RequestParam("artistName") String artistName) {
        playEntryService.addExclusionArtist(artistName);
    }

    @GetMapping("/search-variants")
    public List<ArtistVariantSearchProjection> searchArtistVariants(
            @RequestParam String query,
            @RequestParam(required = false) Integer limit
    ) {
        return playEntryService.searchArtistVariants(query, limit);
    }

}