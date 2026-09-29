package com.example.modele;

import java.util.Date;

public class DailySummarySongDto {

    private String artistName;
    private String songName;
    private String albumName;
    private String imageUrl;
    private Integer nbPlayed;
    private Date datePlayed;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getNbPlayed() {
        return nbPlayed;
    }

    public void setNbPlayed(Integer nbPlayed) {
        this.nbPlayed = nbPlayed;
    }

    public Date getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }
}