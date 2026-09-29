package com.example.modele;

import java.util.Date;

public interface AlbumSummaryProjection {
    String getArtistName();
    String getAlbumName();
    Long getTrackedPlayCount();
    Long getHistoricalPlayCount();
    Date getFirstPlayedAt();
    Date getLastPlayedAt();
    Long getDistinctSongCount();
    String getImageUrl();
}
