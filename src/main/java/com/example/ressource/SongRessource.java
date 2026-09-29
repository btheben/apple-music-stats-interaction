package com.example.ressource;

import com.example.modele.AppleMusicEntry;
import com.example.modele.SongHeroBadgeDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongRessource {

    private Long id;
    private String artistName;
    private String songName;
    private String albumName;
    private Date dateAdded;
    private Date datePlayed;
    private String length;
    private String genre;
    private Integer rating;
    private String imageUrl;
    private List<AppleMusicEntry> listEntries = new ArrayList<>();

    private Integer lastFmRank;

    private Integer currentPlayCount;
    private Integer nextMilestone;
    private Integer playsNeededForNextMilestone;
    private Integer milestoneProgressPercent;

    private List<SongHeroBadgeDto> heroBadges = new ArrayList<>();

    public SongRessource() {
    }

    public SongRessource(AppleMusicEntry entry) {
        if (entry != null) {
            this.id = entry.getId();
            this.artistName = entry.getArtistName();
            this.songName = entry.getSongName();
            this.albumName = entry.getAlbumName();
            this.dateAdded = entry.getDateAdded();
            this.datePlayed = entry.getDatePlayed();
            this.length = entry.getLength();
            this.genre = entry.getGenre();
            this.rating = entry.getRating();
            this.imageUrl = entry.getImageUrl();
            this.currentPlayCount = entry.getNbPlayed();
        }
    }

    public void addHeroBadge(String label, String type, String source) {
        if (label != null && !label.isBlank()) {
            this.heroBadges.add(new SongHeroBadgeDto(label, type, source));
        }
    }

    public Long getId() {
        return id;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongName() {
        return songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public Date getDatePlayed() {
        return datePlayed;
    }

    public String getLength() {
        return length;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<AppleMusicEntry> getListEntries() {
        return listEntries;
    }

    public Integer getLastFmRank() {
        return lastFmRank;
    }

    public Integer getCurrentPlayCount() {
        return currentPlayCount;
    }

    public Integer getNextMilestone() {
        return nextMilestone;
    }

    public Integer getPlaysNeededForNextMilestone() {
        return playsNeededForNextMilestone;
    }

    public Integer getMilestoneProgressPercent() {
        return milestoneProgressPercent;
    }

    public List<SongHeroBadgeDto> getHeroBadges() {
        return heroBadges;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setDatePlayed(Date datePlayed) {
        this.datePlayed = datePlayed;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setListEntries(List<AppleMusicEntry> listEntries) {
        this.listEntries = listEntries;
    }

    public void setLastFmRank(Integer lastFmRank) {
        this.lastFmRank = lastFmRank;
    }

    public void setCurrentPlayCount(Integer currentPlayCount) {
        this.currentPlayCount = currentPlayCount;
    }

    public void setNextMilestone(Integer nextMilestone) {
        this.nextMilestone = nextMilestone;
    }

    public void setPlaysNeededForNextMilestone(Integer playsNeededForNextMilestone) {
        this.playsNeededForNextMilestone = playsNeededForNextMilestone;
    }

    public void setMilestoneProgressPercent(Integer milestoneProgressPercent) {
        this.milestoneProgressPercent = milestoneProgressPercent;
    }

    public void setHeroBadges(List<SongHeroBadgeDto> heroBadges) {
        this.heroBadges = heroBadges;
    }
}