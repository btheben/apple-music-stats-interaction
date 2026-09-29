package com.example.modele;

import java.util.Date;

public interface RediscoveryArtistProjection {
    String getArtistName();
    Long getTotalPlays();
    Date getLastPlayed();
    Integer getDaysSinceLastPlay();
}