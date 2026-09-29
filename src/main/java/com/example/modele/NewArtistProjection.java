package com.example.modele;

import java.util.Date;

public interface NewArtistProjection {
    String getArtistName();
    Long getPlayCount();
    String getImageUrl();

    Date getFirstPlayedDate();
    String getTopSongName();
}