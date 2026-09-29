package com.example.modele;

import java.util.Date;

public interface RecentRediscoveryProjection {
    String getArtistName();
    Date getFirstRecentPlay();
    Date getLastRecentPlay();
    Date getPreviousPlay();
    Integer getDaysWithoutListening();
    Long getRecentPlayCount();
    Long getTotalPlayCount();
    String getLastSongName(); // 👈 ajouté
}