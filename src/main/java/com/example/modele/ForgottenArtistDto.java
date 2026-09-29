package com.example.modele;

import java.util.Date;

public class ForgottenArtistDto {
    private String artistName;
    private Integer totalPlays;
    private Date lastPlayed;
    private Integer daysSinceLastPlay;
    private String imageUrl;

    public ForgottenArtistDto() {
    }

    public ForgottenArtistDto(String artistName, Integer totalPlays, Date lastPlayed, Integer daysSinceLastPlay, String imageUrl) {
        this.artistName = artistName;
        this.totalPlays = totalPlays;
        this.lastPlayed = lastPlayed;
        this.daysSinceLastPlay = daysSinceLastPlay;
        this.imageUrl = imageUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Integer getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(Integer totalPlays) {
        this.totalPlays = totalPlays;
    }

    public Date getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Date lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public Integer getDaysSinceLastPlay() {
        return daysSinceLastPlay;
    }

    public void setDaysSinceLastPlay(Integer daysSinceLastPlay) {
        this.daysSinceLastPlay = daysSinceLastPlay;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}