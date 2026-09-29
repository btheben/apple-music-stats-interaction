package com.example.modele;

public interface UpcomingMilestoneProjection {
    String getArtistName();
    String getSongName();
    String getAlbumName();
    String getImageUrl();
    Integer getCurrentPlayCount();
    Integer getNextMilestone();
    Integer getPlaysNeeded();
    Integer getRecentPlayCount();
}