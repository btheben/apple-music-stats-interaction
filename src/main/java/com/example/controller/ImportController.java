package com.example.controller;

import com.example.service.PlayEntryNormalizationBackfillService;
import com.example.service.PlayEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final PlayEntryService playEntryService;

    public ImportController(PlayEntryService playEntryService) {
        this.playEntryService = playEntryService;
    }

    @GetMapping("/nbFiles")
    public int getNbFiles() {
        return playEntryService.getNbFiles();
    }

    @PostMapping("/loadFiles")
    public void loadFiles() {
        playEntryService.loadFiles();
    }

}