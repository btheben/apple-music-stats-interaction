package com.example.modele;

public interface HIndexArtistProjection {
    String getArtistName();
    Long getPlayCount();
    Integer getRankPosition();
    Integer getHIndex();
    Integer getPlaysNeededForNextH();
    Boolean getInHIndex();
    String getImageUrl();
    String getTopAlbumName();
}