package com.example.modele;

import java.util.Date;

public interface ArtistVariantSearchProjection {
    String getArtistName();
    String getArtistNameNormalized();
    Long getEntryCount();
    Date getFirstPlayedAt();
    Date getLastPlayedAt();
    String getImageUrl();
}