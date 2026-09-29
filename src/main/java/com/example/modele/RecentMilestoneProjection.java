package com.example.modele;

import java.util.Date;

public interface RecentMilestoneProjection {
    String getArtistName();
    String getSongName();
    String getAlbumName();
    String getImageUrl();
    Integer getPreviousPlayCount();
    Integer getCurrentPlayCount();
    Integer getMilestoneReached();
    Date getReachedAt();
    Integer getRecentPlayCount();
}