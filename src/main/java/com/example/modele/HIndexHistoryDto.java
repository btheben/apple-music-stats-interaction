package com.example.modele;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HIndexHistoryDto {

    private Integer hIndex;
    private Date reachedAt;
    private List<HIndexHistoryArtistDto> coreArtists = new ArrayList<>();

    public Integer getHIndex() {
        return hIndex;
    }

    public void setHIndex(Integer hIndex) {
        this.hIndex = hIndex;
    }

    public Date getReachedAt() {
        return reachedAt;
    }

    public void setReachedAt(Date reachedAt) {
        this.reachedAt = reachedAt;
    }

    public List<HIndexHistoryArtistDto> getCoreArtists() {
        return coreArtists;
    }

    public void setCoreArtists(List<HIndexHistoryArtistDto> coreArtists) {
        this.coreArtists = coreArtists;
    }

    public static class HIndexHistoryArtistDto {
        private String artistName;
        private Long playCount;
        private Integer rankPosition;
        private String imageUrl;
        private String topAlbumName;

        public String getArtistName() {
            return artistName;
        }

        public void setArtistName(String artistName) {
            this.artistName = artistName;
        }

        public Long getPlayCount() {
            return playCount;
        }

        public void setPlayCount(Long playCount) {
            this.playCount = playCount;
        }

        public Integer getRankPosition() {
            return rankPosition;
        }

        public void setRankPosition(Integer rankPosition) {
            this.rankPosition = rankPosition;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getTopAlbumName() {
            return topAlbumName;
        }

        public void setTopAlbumName(String topAlbumName) {
            this.topAlbumName = topAlbumName;
        }
    }
}