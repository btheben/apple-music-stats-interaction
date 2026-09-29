package com.example.controller;

import com.example.modele.ArtistSetlistDto;
import com.example.modele.SongLiveStatsDto;
import com.example.service.SetlistFmService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/setlists")
public class SetlistController {

    private final SetlistFmService setlistFmService;

    public SetlistController(SetlistFmService setlistFmService) {
        this.setlistFmService = setlistFmService;
    }

    @GetMapping("/artist/{artistName}")
    public ArtistSetlistDto getArtistSetlist(@PathVariable String artistName) {
        return setlistFmService.getArtistSetlistData(artistName);
    }

    @GetMapping("/song")
    public SongLiveStatsDto getSongLiveStats(
            @RequestParam("artistName") String artistName,
            @RequestParam("songName") String songName) {
        return setlistFmService.getSongLiveStats(artistName, songName);
    }
}
