package com.example.modele;

import java.util.Date;

public interface RecentNewArtistProjection {
    String getArtistName();
    Integer getPlayCount();
    Date getFirstPlayDate();
    String getImageUrl();
}