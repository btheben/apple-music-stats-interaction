package com.example.modele;

public class DashboardTopArtistDto {
    private String artistName;
    private Long nbPlayed;
    private String imageUrl;


    public DashboardTopArtistDto() {
    }

    public DashboardTopArtistDto(String artistName, Long nbPlayed, String imageUrl) {
        this.artistName = artistName;
        this.nbPlayed = nbPlayed;
        this.imageUrl = imageUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Long getNbPlayed() {
        return nbPlayed;
    }

    public void setNbPlayed(Long nbPlayed) {
        this.nbPlayed = nbPlayed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}