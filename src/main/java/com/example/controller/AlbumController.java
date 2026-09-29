package com.example.controller;

import com.example.modele.AlbumDetails;
import com.example.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/details")
    public ResponseEntity<AlbumDetails> getAlbumDetails(@RequestParam String artistName,
                                                        @RequestParam String albumName) {
        AlbumDetails details = albumService.getAlbumDetails(artistName, albumName);

        if (details == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(details);
    }
}