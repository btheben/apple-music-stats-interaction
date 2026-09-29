package com.example.modele;

import java.util.Date;

public class AlbumPlayEntry {
    private String songName;
    private Date datePlayed;
    private Integer nbPlayed;

    public AlbumPlayEntry(String songName, Date datePlayed, Integer nbPlayed) {
        this.songName = songName;
        this.datePlayed = datePlayed;
        this.nbPlayed = nbPlayed;
    }

    public String getSongName() {
        return songName;
    }

    public Date getDatePlayed() {
        return datePlayed;
    }

    public Integer getNbPlayed() {
        return nbPlayed;
    }
}