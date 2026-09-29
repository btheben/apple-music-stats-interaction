package com.example.modele;

import java.util.Date;

public class Song {
    private String artistName;
    private String songName;
    private String albumName;
    private Date dateAdded;
    private Date datePlayed;
    private String length;
    private int nbPlayed;
    private String genre;

    public Song(String artistName, String songName, String albumName, Date dateAdded, Date datePlayed, String length, int nbPlayed) {
        this.artistName = artistName;
        this.songName = songName;
        this.albumName = albumName;
        this.dateAdded = dateAdded;
        this.datePlayed = datePlayed;
        this.length = length;
        this.nbPlayed = nbPlayed;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public int getNbPlayed() {
        return nbPlayed;
    }

    public void setNbPlayed(int nbPlayed) {
        this.nbPlayed = nbPlayed;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Date getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }

    @Override
    public String toString() {
        return "AppleMusicEntry{" +
                "artistName='" + artistName + '\'' +
                ", songName='" + songName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", dateAdded=" + dateAdded +
                ", length='" + length + '\'' +
                ", nbPlayed=" + nbPlayed +
                '}';
    }
}
