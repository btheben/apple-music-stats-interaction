package com.example.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "artist")
public class ArtistKeys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ARTIST_NAME", nullable = false)
    private String artistName;

    @Column(name = "SPOTIFY_KEY", nullable = false)
    private String spotifyKey;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSpotifyKey() {
        return spotifyKey;
    }

    public void setSpotifyKey(String spotifyKey) {
        this.spotifyKey = spotifyKey;
    }
}
