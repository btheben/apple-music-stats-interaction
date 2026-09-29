package com.example.modele;

import java.util.Date;

public interface DailySummarySongProjection {
    String getArtistName();
    String getSongName();
    String getAlbumName();
    String getImageUrl();
    Integer getNbPlayed();
    Date getDatePlayed();
}