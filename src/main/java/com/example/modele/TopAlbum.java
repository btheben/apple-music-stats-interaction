package com.example.modele;

public class TopAlbum {
    private String artistName;
    private String albumName;
    private long nbPlayed;
    private String imageUrl;

    public TopAlbum(String artistName, String albumName, long nbPlayed) {
        this.artistName = artistName;
        this.albumName = albumName;
        this.nbPlayed = nbPlayed;
    }

    public TopAlbum(String artistName, String albumName, long nbPlayed, String imageUrl) {
        this.artistName = artistName;
        this.albumName = albumName;
        this.nbPlayed = nbPlayed;
        this.imageUrl = imageUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
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