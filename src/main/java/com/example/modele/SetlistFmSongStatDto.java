package com.example.modele;

public class SetlistFmSongStatDto {

    private String songName;
    private int timesPlayed;
    private Integer liveRank;
    private boolean inLibrary;

    public SetlistFmSongStatDto() {
    }

    public SetlistFmSongStatDto(String songName, int timesPlayed, Integer liveRank, boolean inLibrary) {
        this.songName = songName;
        this.timesPlayed = timesPlayed;
        this.liveRank = liveRank;
        this.inLibrary = inLibrary;
    }

    public String getSongName() {
        return songName;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public Integer getLiveRank() {
        return liveRank;
    }

    public boolean isInLibrary() {
        return inLibrary;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }

    public void setLiveRank(Integer liveRank) {
        this.liveRank = liveRank;
    }

    public void setInLibrary(boolean inLibrary) {
        this.inLibrary = inLibrary;
    }
}