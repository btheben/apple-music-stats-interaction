package com.example.modele;

import java.util.Date;

public interface DailySummaryArtistProjection {
    String getArtistName();
    String getSongName();
    String getAlbumName();
    String getImageUrl();
    Integer getNbPlayed();
    Date getDatePlayed();
    Integer getGapDays();
}