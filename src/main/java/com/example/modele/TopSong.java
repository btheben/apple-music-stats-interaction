package com.example.modele;

public class TopSong {
    private String artistName;
    private String songName;
    private long nbPlayed;
    private String imageUrl;

    public TopSong(String artistName, String songName, long nbPlayed) {
        this.artistName = artistName;
        this.songName = songName;
        this.nbPlayed = nbPlayed;
    }

    public TopSong(String artistName, String songName, long nbPlayed, String imageUrl) {
        this.artistName = artistName;
        this.songName = songName;
        this.nbPlayed = nbPlayed;
        this.imageUrl = imageUrl;
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

    public long getNbPlayed() {
        return nbPlayed;
    }

    public void setNbPlayed(long nbPlayed) {
        this.nbPlayed = nbPlayed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}