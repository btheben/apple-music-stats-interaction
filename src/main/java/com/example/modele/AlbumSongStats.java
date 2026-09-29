package com.example.modele;

import java.util.Date;

public class AlbumSongStats {
    private String songName;
    private Long playCount;
    private Date firstPlayedAt;
    private Date lastPlayedAt;
    private Integer rankPosition;

    public AlbumSongStats() {
    }

    public AlbumSongStats(String songName, Long playCount, Date firstPlayedAt, Date lastPlayedAt) {
        this.songName = songName;
        this.playCount = playCount;
        this.firstPlayedAt = firstPlayedAt;
        this.lastPlayedAt = lastPlayedAt;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
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

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }
}