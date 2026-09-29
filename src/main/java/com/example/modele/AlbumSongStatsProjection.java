package com.example.modele;

import java.util.Date;

public interface AlbumSongStatsProjection {
    String getSongName();
    Integer getPlayCount();
    Date getFirstPlayedAt();
    Date getLastPlayedAt();
}
