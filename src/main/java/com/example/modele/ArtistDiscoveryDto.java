package com.example.modele;

import java.util.List;

public class ArtistDiscoveryDto {
    private String artistName;
    private List<SimilarArtistDto> similarArtists;
    private List<RecommendedTrackDto> recommendedTracks;

    public ArtistDiscoveryDto() {
    }

    public ArtistDiscoveryDto(String artistName,
                              List<SimilarArtistDto> similarArtists,
                              List<RecommendedTrackDto> recommendedTracks) {
        this.artistName = artistName;
        this.similarArtists = similarArtists;
        this.recommendedTracks = recommendedTracks;
    }

    public String getArtistName() {
        return artistName;
    }

    public List<SimilarArtistDto> getSimilarArtists() {
        return similarArtists;
    }

    public List<RecommendedTrackDto> getRecommendedTracks() {
        return recommendedTracks;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setSimilarArtists(List<SimilarArtistDto> similarArtists) {
        this.similarArtists = similarArtists;
    }

    public void setRecommendedTracks(List<RecommendedTrackDto> recommendedTracks) {
        this.recommendedTracks = recommendedTracks;
    }
}
