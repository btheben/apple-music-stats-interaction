package com.example.controller;

import com.example.modele.ArtistDiscoveryDto;
import com.example.modele.ArtistRecommendationDto;
import com.example.modele.RediscoveryArtistProjection;
import com.example.modele.RecentRediscoveryProjection;
import com.example.service.ArtistDiscoveryService;
import com.example.service.PlayEntryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/discovery")
public class DiscoveryController extends BaseDateController {

    private final ArtistDiscoveryService artistDiscoveryService;
    private final PlayEntryService playEntryService;

    public DiscoveryController(ArtistDiscoveryService artistDiscoveryService,
                               PlayEntryService playEntryService) {
        this.artistDiscoveryService = artistDiscoveryService;
        this.playEntryService = playEntryService;
    }

    @GetMapping("/artist/{artistName}")
    public ArtistDiscoveryDto getArtistDiscovery(@PathVariable String artistName) {
        return artistDiscoveryService.getArtistDiscovery(artistName);
    }

    @GetMapping("/recommendations")
    public List<ArtistRecommendationDto> getLibraryRecommendations(
            @RequestParam(value = "minSourcePlays", required = false) Integer minSourcePlays,
            @RequestParam(value = "maxLocalPlays", required = false) Integer maxLocalPlays) {

        int safeMinSource = minSourcePlays != null ? minSourcePlays : 20;
        int safeMaxLocal = maxLocalPlays != null ? maxLocalPlays : 3;

        return artistDiscoveryService.getLibraryRecommendations(safeMinSource, safeMaxLocal);
    }

    @GetMapping("/rediscovery/random")
    public List<RediscoveryArtistProjection> getRandomRediscoveryArtists(
            @RequestParam(value = "minPlays", required = false) Integer minPlays,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) {

        int safeMinPlays = minPlays != null ? minPlays : 20;
        int safeLimit = limitValue != null ? limitValue : 10;

        return playEntryService.getRandomRediscoveryArtists(safeMinPlays, safeLimit);
    }

    @GetMapping("/rediscovery/recent")
    public List<RecentRediscoveryProjection> getRecentRediscoveries(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString,
            @RequestParam(value = "minGapDays", required = false) Integer minGapDays,
            @RequestParam(value = "minTotalPlays", required = false) Integer minTotalPlays,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);

        return playEntryService.getRecentRediscoveries(
                range.getDateStart(),
                range.getDateEnd(),
                minGapDays != null ? minGapDays : 180,
                minTotalPlays != null ? minTotalPlays : 5,
                limitValue != null ? limitValue : 20
        );
    }
}
