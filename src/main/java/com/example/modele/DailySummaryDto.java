package com.example.modele;

import java.util.Date;

public class DailySummaryDto {

    private Integer totalEntries;
    private Integer distinctArtists;
    private Integer distinctSongs;

    private SummaryEntryDto topSong;
    private SummaryEntryDto newestArtist;
    private SummaryEntryDto rediscoveryArtist;

    public Integer getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(Integer totalEntries) {
        this.totalEntries = totalEntries;
    }

    public Integer getDistinctArtists() {
        return distinctArtists;
    }

    public void setDistinctArtists(Integer distinctArtists) {
        this.distinctArtists = distinctArtists;
    }

    public Integer getDistinctSongs() {
        return distinctSongs;
    }

    public void setDistinctSongs(Integer distinctSongs) {
        this.distinctSongs = distinctSongs;
    }

    public SummaryEntryDto getTopSong() {
        return topSong;
    }

    public void setTopSong(SummaryEntryDto topSong) {
        this.topSong = topSong;
    }

    public SummaryEntryDto getNewestArtist() {
        return newestArtist;
    }

    public void setNewestArtist(SummaryEntryDto newestArtist) {
        this.newestArtist = newestArtist;
    }

    public SummaryEntryDto getRediscoveryArtist() {
        return rediscoveryArtist;
    }

    public void setRediscoveryArtist(SummaryEntryDto rediscoveryArtist) {
        this.rediscoveryArtist = rediscoveryArtist;
    }

    public static class SummaryEntryDto {
        private String artistName;
        private String songName;
        private String albumName;
        private String imageUrl;
        private Integer nbPlayed;
        private Date datePlayed;
        private Integer gapDays;
        private String label;

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

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Integer getNbPlayed() {
            return nbPlayed;
        }

        public void setNbPlayed(Integer nbPlayed) {
            this.nbPlayed = nbPlayed;
        }

        public Date getDatePlayed() {
            return datePlayed;
        }

        public void setDatePlayed(Date datePlayed) {
            this.datePlayed = datePlayed;
        }

        public Integer getGapDays() {
            return gapDays;
        }

        public void setGapDays(Integer gapDays) {
            this.gapDays = gapDays;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}