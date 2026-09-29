package com.example.modele;

public class DashboardHIndexSummaryDto {

    private Integer hIndex;
    private Integer artistsNeededForNextH;

    public DashboardHIndexSummaryDto() {
    }

    public DashboardHIndexSummaryDto(Integer hIndex, Integer artistsNeededForNextH) {
        this.hIndex = hIndex;
        this.artistsNeededForNextH = artistsNeededForNextH;
    }

    public Integer getHIndex() {
        return hIndex;
    }

    public void setHIndex(Integer hIndex) {
        this.hIndex = hIndex;
    }

    public Integer getArtistsNeededForNextH() {
        return artistsNeededForNextH;
    }

    public void setArtistsNeededForNextH(Integer artistsNeededForNextH) {
        this.artistsNeededForNextH = artistsNeededForNextH;
    }
}