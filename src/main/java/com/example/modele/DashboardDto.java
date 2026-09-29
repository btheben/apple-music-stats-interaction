package com.example.modele;

import java.util.List;

public class DashboardDto {

    private List<TopArtist> topArtists;
    private List<TopSong> topSongs;
    private List<TopAlbum> topAlbums;

    private List<AppleMusicEntry> recentActivity;

    private List<UpcomingMilestoneProjection> upcomingMilestones;

    private List<TrendingArtistDto> trendingArtists;

    private List<ArtistRecommendationDto> discoveryArtists;
    private List<RediscoveryArtistProjection> rediscoveryArtists;

    public List<TopArtist> getTopArtists() {
        return topArtists;
    }

    public void setTopArtists(List<TopArtist> topArtists) {
        this.topArtists = topArtists;
    }

    public List<TopSong> getTopSongs() {
        return topSongs;
    }

    public void setTopSongs(List<TopSong> topSongs) {
        this.topSongs = topSongs;
    }

    public List<TopAlbum> getTopAlbums() {
        return topAlbums;
    }

    public void setTopAlbums(List<TopAlbum> topAlbums) {
        this.topAlbums = topAlbums;
    }

    public List<AppleMusicEntry> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<AppleMusicEntry> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public List<UpcomingMilestoneProjection> getUpcomingMilestones() {
        return upcomingMilestones;
    }

    public void setUpcomingMilestones(List<UpcomingMilestoneProjection> upcomingMilestones) {
        this.upcomingMilestones = upcomingMilestones;
    }

    public List<TrendingArtistDto> getTrendingArtists() {
        return trendingArtists;
    }

    public void setTrendingArtists(List<TrendingArtistDto> trendingArtists) {
        this.trendingArtists = trendingArtists;
    }

    public List<ArtistRecommendationDto> getDiscoveryArtists() {
        return discoveryArtists;
    }

    public void setDiscoveryArtists(List<ArtistRecommendationDto> discoveryArtists) {
        this.discoveryArtists = discoveryArtists;
    }

    public List<RediscoveryArtistProjection> getRediscoveryArtists() {
        return rediscoveryArtists;
    }

    public void setRediscoveryArtists(List<RediscoveryArtistProjection> rediscoveryArtists) {
        this.rediscoveryArtists = rediscoveryArtists;
    }
}