package com.example.modele;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity
public class TopArtist {

    @Id
    private String artistName;
    private long nbPlayed;
    private String songName;
    private Timestamp lastPlayed;
    private String imageUrl;

    public TopArtist() {
    }

    public TopArtist(String artistName, long nbPlayed) {
        this.artistName = artistName;
        this.nbPlayed = nbPlayed;
    }

    public TopArtist(String artistName, long nbPlayed, String imageUrl) {
        this.artistName = artistName;
        this.nbPlayed = nbPlayed;
        this.imageUrl = imageUrl;
    }

    public TopArtist(String artistName, long nbPlayed, String songName, Timestamp lastPlayed) {
        this.artistName = artistName;
        this.nbPlayed = nbPlayed;
        this.songName = songName;
        this.lastPlayed = lastPlayed;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public long getNbPlayed() {
        return nbPlayed;
    }

    public void setNbPlayed(long nbPlayed) {
        this.nbPlayed = nbPlayed;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Timestamp getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Timestamp lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}