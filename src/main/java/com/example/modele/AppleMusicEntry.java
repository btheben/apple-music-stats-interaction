package com.example.modele;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PLAY_ENTRY")
public class AppleMusicEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ARTIST_NAME", nullable = false)
    private String artistName;

    @Column(name = "SONG_NAME", nullable = false)
    private String songName;

    @Column(name = "ALBUM_NAME", nullable = false)
    private String albumName;

    @Column(name = "DATE_ADDED", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAdded;

    @Column(name = "DATE_PLAYED", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePlayed;

    @Column(name = "LENGTH", nullable = false)
    private String length;

    @Column(name = "NB_PLAYED", nullable = false)
    private int nbPlayed;

    @Column(name = "GENRE")
    private String genre;

    @Column(name = "RATING")
    private Integer rating;

    @Column(name = "IMAGE_URL")
    private String imageUrl;

    @Column(name = "artist_name_normalized")
    private String artistNameNormalized;

    @Transient
    private Long artistYearPlayNumber;


    public Long getArtistYearPlayNumber() {
        return artistYearPlayNumber;
    }

    public void setArtistYearPlayNumber(Long artistYearPlayNumber) {
        this.artistYearPlayNumber = artistYearPlayNumber;
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

    public Date getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getArtistNameNormalized() {
        return artistNameNormalized;
    }

    public void setArtistNameNormalized(String artistNameNormalized) {
        this.artistNameNormalized = artistNameNormalized;
    }

    @Override
    public String toString() {
        return "AppleMusicEntry{" +
                "artistName='" + artistName + '\'' +
                ", songName='" + songName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", dateAdded=" + dateAdded +
                ", datePlayed=" + datePlayed +
                ", length='" + length + '\'' +
                ", nbPlayed=" + nbPlayed +
                '}';
    }
}
