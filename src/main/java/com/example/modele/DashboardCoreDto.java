package com.example.modele;

import java.util.List;

public class DashboardCoreDto {

    private List<DashboardTopArtistDto> topArtists;
    private List<TopSong> topSongs;
    private List<AppleMusicEntry> recentActivity;

    private DashboardHIndexSummaryDto hIndexSummary;
    private List<HIndexArtistProjection> hIndexTargets;

    private List<RecentMilestoneProjection> recentMilestones;
    private List<TrendingArtistDto> trendingArtists;

    private RecentRediscoveryProjection latestRediscovery;

    public RecentRediscoveryProjection getLatestRediscovery() {
        return latestRediscovery;
    }

    public void setLatestRediscovery(RecentRediscoveryProjection latestRediscovery) {
        this.latestRediscovery = latestRediscovery;
    }

    public List<DashboardTopArtistDto> getTopArtists() {
        return topArtists;
    }

    public void setTopArtists(List<DashboardTopArtistDto> topArtists) {
        this.topArtists = topArtists;
    }

    public List<TopSong> getTopSongs() {
        return topSongs;
    }

    public void setTopSongs(List<TopSong> topSongs) {
        this.topSongs = topSongs;
    }

    public List<AppleMusicEntry> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<AppleMusicEntry> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public DashboardHIndexSummaryDto getHIndexSummary() {
        return hIndexSummary;
    }

    public void setHIndexSummary(DashboardHIndexSummaryDto hIndexSummary) {
        this.hIndexSummary = hIndexSummary;
    }

    public List<HIndexArtistProjection> getHIndexTargets() {
        return hIndexTargets;
    }

    public void setHIndexTargets(List<HIndexArtistProjection> hIndexTargets) {
        this.hIndexTargets = hIndexTargets;
    }

    public List<RecentMilestoneProjection> getRecentMilestones() {
        return recentMilestones;
    }

    public void setRecentMilestones(List<RecentMilestoneProjection> recentMilestones) {
        this.recentMilestones = recentMilestones;
    }

    public List<TrendingArtistDto> getTrendingArtists() {
        return trendingArtists;
    }

    public void setTrendingArtists(List<TrendingArtistDto> trendingArtists) {
        this.trendingArtists = trendingArtists;
    }
}