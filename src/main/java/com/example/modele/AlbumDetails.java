package com.example.modele;

import java.util.Date;
import java.util.List;

public class AlbumDetails {
    private String artistName;
    private String albumName;
    private Long historicalPlayCount;
    private Long trackedPlayCount;
    private Date firstPlayedAt;
    private Date lastPlayedAt;
    private Long distinctSongCount;
    private String imageUrl;
    private Integer artistAlbumRank;
    private List<AlbumSongStats> topSongs;
    private List<AlbumPlayEntry> recentPlays;

    public AlbumDetails() {
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

    public Long getHistoricalPlayCount() {
        return historicalPlayCount;
    }

    public void setHistoricalPlayCount(Long historicalPlayCount) {
        this.historicalPlayCount = historicalPlayCount;
    }

    public Long getTrackedPlayCount() {
        return trackedPlayCount;
    }

    public void setTrackedPlayCount(Long trackedPlayCount) {
        this.trackedPlayCount = trackedPlayCount;
    }

    public Date getFirstPlayedAt() {
        return firstPlayedAt;
    }

    public void setFirstPlayedAt(Date firstPlayedAt) {
        this.firstPlayedAt = firstPlayedAt;
    }

    public Date getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(Date lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }

    public Long getDistinctSongCount() {
        return distinctSongCount;
    }

    public void setDistinctSongCount(Long distinctSongCount) {
        this.distinctSongCount = distinctSongCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getArtistAlbumRank() {
        return artistAlbumRank;
    }

    public void setArtistAlbumRank(Integer artistAlbumRank) {
        this.artistAlbumRank = artistAlbumRank;
    }

    public List<AlbumSongStats> getTopSongs() {
        return topSongs;
    }

    public void setTopSongs(List<AlbumSongStats> topSongs) {
        this.topSongs = topSongs;
    }

    public List<AlbumPlayEntry> getRecentPlays() {
        return recentPlays;
    }

    public void setRecentPlays(List<AlbumPlayEntry> recentPlays) {
        this.recentPlays = recentPlays;
    }
}