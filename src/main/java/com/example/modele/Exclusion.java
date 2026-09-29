package com.example.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "exclusion_artist")
public class Exclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ARTIST_NAME", nullable = false)
    private String artistName;

    @Column(name = "artist_name_normalized")
    private String artistNameNormalized;

    public String getArtistNameNormalized() {
        return artistNameNormalized;
    }

    public void setArtistNameNormalized(String artistNameNormalized) {
        this.artistNameNormalized = artistNameNormalized;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
