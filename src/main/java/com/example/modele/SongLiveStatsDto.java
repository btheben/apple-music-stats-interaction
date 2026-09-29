package com.example.modele;

public class SongLiveStatsDto {

    private String artistName;
    private String songName;
    private boolean apiEnabled;
    private boolean playedLive;
    private Integer liveRank;
    private Integer timesPlayedLive;
    private String lastSeenLiveDate;
    private String badgeLabel;

    public SongLiveStatsDto() {
    }

    public SongLiveStatsDto(String artistName, String songName, boolean apiEnabled,
                            boolean playedLive, Integer liveRank,
                            Integer timesPlayedLive, String lastSeenLiveDate,
                            String badgeLabel) {
        this.artistName = artistName;
        this.songName = songName;
        this.apiEnabled = apiEnabled;
        this.playedLive = playedLive;
        this.liveRank = liveRank;
        this.timesPlayedLive = timesPlayedLive;
        this.lastSeenLiveDate = lastSeenLiveDate;
        this.badgeLabel = badgeLabel;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongName() {
        return songName;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public boolean isPlayedLive() {
        return playedLive;
    }

    public Integer getLiveRank() {
        return liveRank;
    }

    public Integer getTimesPlayedLive() {
        return timesPlayedLive;
    }

    public String getLastSeenLiveDate() {
        return lastSeenLiveDate;
    }

    public String getBadgeLabel() {
        return badgeLabel;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }

    public void setPlayedLive(boolean playedLive) {
        this.playedLive = playedLive;
    }

    public void setLiveRank(Integer liveRank) {
        this.liveRank = liveRank;
    }

    public void setTimesPlayedLive(Integer timesPlayedLive) {
        this.timesPlayedLive = timesPlayedLive;
    }

    public void setLastSeenLiveDate(String lastSeenLiveDate) {
        this.lastSeenLiveDate = lastSeenLiveDate;
    }

    public void setBadgeLabel(String badgeLabel) {
        this.badgeLabel = badgeLabel;
    }
}