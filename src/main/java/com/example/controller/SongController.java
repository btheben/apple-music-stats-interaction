package com.example.controller;

import com.example.ressource.SongRessource;
import com.example.service.PlayEntryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/songs")
@CrossOrigin(
        origins = {
                "http://localhost:4200",
                "http://192.168.2.13:4200"
        },
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.OPTIONS
        }
)
public class SongController {

    private final PlayEntryService playEntryService;

    public SongController(PlayEntryService playEntryService) {
        this.playEntryService = playEntryService;
    }

    @GetMapping("/detail")
    public SongRessource getSong(
            @RequestParam("artistName") String artistName,
            @RequestParam("songName") String songName) {
        return playEntryService.getByArtistAndSong(artistName, songName);
    }

    @PutMapping("/metadata")
    public int updateSongMetadata(
            @RequestParam("artistName") String artistName,
            @RequestParam("oldSongName") String oldSongName,
            @RequestParam("newSongName") String newSongName,
            @RequestParam("newAlbumName") String newAlbumName) {
        return playEntryService.updateSongMetadata(
                artistName,
                oldSongName,
                newSongName,
                newAlbumName
        );
    }

    // Route legacy au cas où une ancienne partie du front l'utilise encore
    @PostMapping("/updateSongMetadata")
    public int updateSongMetadataLegacy(
            @RequestParam("artistName") String artistName,
            @RequestParam("oldSongName") String oldSongName,
            @RequestParam("newSongName") String newSongName,
            @RequestParam("newAlbumName") String newAlbumName) {
        return playEntryService.updateSongMetadata(
                artistName,
                oldSongName,
                newSongName,
                newAlbumName
        );
    }

    // Réponse explicite au preflight OPTIONS
    @RequestMapping(value = "/metadata", method = RequestMethod.OPTIONS)
    public void metadataOptions() {
        // vide volontairement
    }
}