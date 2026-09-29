package com.example.service;

import com.example.depot.PlayEntryDepot;
import com.example.modele.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlbumService {

    private final PlayEntryDepot playEntryDepot;

    public AlbumService(PlayEntryDepot playEntryDepot) {
        this.playEntryDepot = playEntryDepot;
    }

    public AlbumDetails getAlbumDetails(String artistName, String albumName) {
        AlbumSummaryProjection summary = playEntryDepot.getAlbumSummary(artistName, albumName);

        if (summary == null || summary.getAlbumName() == null) {
            return null;
        }

        AlbumDetails details = new AlbumDetails();
        details.setArtistName(summary.getArtistName());
        details.setAlbumName(summary.getAlbumName());
        details.setHistoricalPlayCount(summary.getHistoricalPlayCount());
        details.setTrackedPlayCount(summary.getTrackedPlayCount());
        details.setFirstPlayedAt(summary.getFirstPlayedAt());
        details.setLastPlayedAt(summary.getLastPlayedAt());
        details.setDistinctSongCount(summary.getDistinctSongCount());
        details.setImageUrl(summary.getImageUrl());

        List<AlbumSongStatsProjection> songProjections =
                playEntryDepot.getAlbumSongStats(artistName, albumName);

        List<AlbumSongStats> topSongs = new ArrayList<>();
        for (int i = 0; i < songProjections.size(); i++) {
            AlbumSongStatsProjection projection = songProjections.get(i);

            AlbumSongStats stat = new AlbumSongStats();
            stat.setSongName(projection.getSongName());
            stat.setPlayCount(projection.getPlayCount().longValue());
            stat.setFirstPlayedAt(projection.getFirstPlayedAt());
            stat.setLastPlayedAt(projection.getLastPlayedAt());
            stat.setRankPosition(i + 1);

            topSongs.add(stat);
        }
        details.setTopSongs(topSongs);

        List<AlbumPlayEntry> recentPlays =
                playEntryDepot.getRecentPlaysForAlbum(artistName, albumName, PageRequest.of(0, 20));
        details.setRecentPlays(recentPlays);

        AlbumRankProjection rankProjection =
                playEntryDepot.getAlbumRankForArtist(artistName, albumName);

        if (rankProjection != null) {
            details.setArtistAlbumRank(rankProjection.getRankPosition());
        }

        return details;
    }
}