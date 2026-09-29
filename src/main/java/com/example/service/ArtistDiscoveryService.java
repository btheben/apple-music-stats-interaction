package com.example.service;

import com.example.depot.PlayEntryDepot;
import com.example.modele.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArtistDiscoveryService {

    private final PlayEntryDepot playEntryDepot;
    private final LastFmService lastFmService;

    public ArtistDiscoveryService(PlayEntryDepot playEntryDepot, LastFmService lastFmService) {
        this.playEntryDepot = playEntryDepot;
        this.lastFmService = lastFmService;
    }

    public ArtistDiscoveryDto getArtistDiscovery(String artistName) {
        Map<String, Integer> localPlayMap = getLocalSongPlayMap(artistName);

        List<RecommendedTrackDto> lastFmTopTracks = lastFmService.getTopTracksFromLastFm(artistName, 12);
        List<SimilarArtistDto> similarArtists = lastFmService.getSimilarArtists(artistName, 6)
                .stream()
                .map(this::enrichSimilarArtist)
                .collect(Collectors.toList());

        List<RecommendedTrackDto> recommendedTracks = lastFmTopTracks.stream()
                .map(track -> {
                    String normalizedTrackName = normalize(track.getName());
                    int localPlayCount = localPlayMap.getOrDefault(normalizedTrackName, 0);

                    return new RecommendedTrackDto(
                            track.getName(),
                            track.getUrl(),
                            localPlayCount,
                            localPlayCount > 0,
                            track.getLastFmRank()
                    );
                })
                .sorted(Comparator
                        .comparingInt(RecommendedTrackDto::getLocalPlayCount)
                        .thenComparing(track -> track.getLastFmRank() != null ? track.getLastFmRank() : Integer.MAX_VALUE)
                        .thenComparing(RecommendedTrackDto::getName))
                .collect(Collectors.toList());

        return new ArtistDiscoveryDto(artistName, similarArtists, recommendedTracks);
    }

    private SimilarArtistDto enrichSimilarArtist(SimilarArtistDto artist) {
        ArtistPresenceProjection presence = playEntryDepot.getArtistPresence(artist.getName());

        boolean presentInLibrary = presence != null && presence.getPlayCount() != null && presence.getPlayCount() > 0;
        int localPlayCount = presentInLibrary ? presence.getPlayCount().intValue() : 0;

        artist.setPresentInLibrary(presentInLibrary);
        artist.setLocalPlayCount(localPlayCount);

        return artist;
    }

    private Map<String, Integer> getLocalSongPlayMap(String artistName) {
        List<PlayEntryDepot.ArtistSongPlayCountProjection> rows = playEntryDepot.getSongPlayCountsByArtist(artistName);
        Map<String, Integer> result = new HashMap<>();

        for (PlayEntryDepot.ArtistSongPlayCountProjection row : rows) {
            result.put(
                    normalize(row.getSongName()),
                    row.getPlayCount() != null ? row.getPlayCount().intValue() : 0
            );
        }

        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public List<ArtistRecommendationDto> getLibraryRecommendations(int minSourcePlays, int maxLocalPlaysForCandidate) {
        List<TopArtistProjection> sourceArtists = playEntryDepot.getTopArtistsForRecommendations(minSourcePlays, 15);

        Map<String, ArtistRecommendationDto> dedup = new LinkedHashMap<>();

        for (TopArtistProjection source : sourceArtists) {
            List<SimilarArtistDto> similar = lastFmService.getSimilarArtists(source.getArtistName(), 5);

            for (SimilarArtistDto candidate : similar) {
                ArtistPresenceProjection presence = playEntryDepot.getArtistPresence(candidate.getName());
                int localCount = presence != null && presence.getPlayCount() != null ? presence.getPlayCount().intValue() : 0;

                if (localCount <= maxLocalPlaysForCandidate) {
                    String key = normalize(candidate.getName());

                    if (!dedup.containsKey(key)) {
                        dedup.put(key, new ArtistRecommendationDto(
                                source.getArtistName(),
                                candidate.getName(),
                                candidate.getUrl(),
                                candidate.getImageUrl(),
                                candidate.getMatch(),
                                localCount > 0,
                                localCount
                        ));
                    }
                }
            }
        }

        return dedup.values().stream()
                .sorted(Comparator
                        .comparingInt(ArtistRecommendationDto::getLocalPlayCount)
                        .thenComparing((a, b) -> Double.compare(
                                parseScore(b.getSimilarityScore()),
                                parseScore(a.getSimilarityScore())
                        )))
                .limit(20)
                .collect(Collectors.toList());
    }

    private double parseScore(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return 0.0;
        }
    }
}
