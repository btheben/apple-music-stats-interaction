package com.example.modele;

import java.util.List;

public class Artist {
    private String artistName;
    private String spotifyKey;
    private Integer totalNbPlayed;
    private Integer position;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    List<Song> listSongs;
    List<AppleMusicEntry> listAppleMusicEntry;
    List<Artist> listRelatedArtists;

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

    public List<Song> getListSongs() {
        return listSongs;
    }

    public void setListSongs(List<Song> listSongs) {
        this.listSongs = listSongs;
    }

    public List<AppleMusicEntry> getListAppleMusicEntry() {
        return listAppleMusicEntry;
    }

    public void setListAppleMusicEntry(List<AppleMusicEntry> listAppleMusicEntry) {
        this.listAppleMusicEntry = listAppleMusicEntry;
    }

    public Integer getTotalNbPlayed() {
        return totalNbPlayed;
    }

    public void setTotalNbPlayed(Integer totalNbPlayed) {
        this.totalNbPlayed = totalNbPlayed;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<Artist> getListRelatedArtists() {
        return listRelatedArtists;
    }

    public void setListRelatedArtists(List<Artist> listRelatedArtists) {
        this.listRelatedArtists = listRelatedArtists;
    }
}
