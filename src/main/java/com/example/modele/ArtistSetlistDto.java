package com.example.modele;

import java.util.ArrayList;
import java.util.List;

public class ArtistSetlistDto {

    private String artistName;
    private String artistMbid;
    private boolean apiEnabled;
    private SetlistFmArtistSetlistDto latestSetlist;
    private List<SetlistFmSongStatDto> topLiveSongs = new ArrayList<>();

    public ArtistSetlistDto() {
    }

    public ArtistSetlistDto(String artistName, String artistMbid, boolean apiEnabled,
                            SetlistFmArtistSetlistDto latestSetlist,
                            List<SetlistFmSongStatDto> topLiveSongs) {
        this.artistName = artistName;
        this.artistMbid = artistMbid;
        this.apiEnabled = apiEnabled;
        this.latestSetlist = latestSetlist;
        this.topLiveSongs = topLiveSongs;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public SetlistFmArtistSetlistDto getLatestSetlist() {
        return latestSetlist;
    }

    public List<SetlistFmSongStatDto> getTopLiveSongs() {
        return topLiveSongs;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }

    public void setLatestSetlist(SetlistFmArtistSetlistDto latestSetlist) {
        this.latestSetlist = latestSetlist;
    }

    public void setTopLiveSongs(List<SetlistFmSongStatDto> topLiveSongs) {
        this.topLiveSongs = topLiveSongs;
    }
}