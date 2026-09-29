package com.example.modele;

public class TrendingArtistDto {

    private String artistName;
    private Integer currentPlays;
    private Integer previousPlays;
    private Integer delta;
    private Integer percentChange;

    public TrendingArtistDto() {
    }

    public TrendingArtistDto(String artistName, Integer currentPlays, Integer previousPlays, Integer delta, Integer percentChange) {
        this.artistName = artistName;
        this.currentPlays = currentPlays;
        this.previousPlays = previousPlays;
        this.delta = delta;
        this.percentChange = percentChange;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Integer getCurrentPlays() {
        return currentPlays;
    }

    public void setCurrentPlays(Integer currentPlays) {
        this.currentPlays = currentPlays;
    }

    public Integer getPreviousPlays() {
        return previousPlays;
    }

    public void setPreviousPlays(Integer previousPlays) {
        this.previousPlays = previousPlays;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(Integer percentChange) {
        this.percentChange = percentChange;
    }
}