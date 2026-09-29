package com.example.modele;

import jakarta.persistence.*;

public class RelatedArtist {

    private Long id;

    private String artistName;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
