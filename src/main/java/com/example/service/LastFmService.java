package com.example.service;

import com.example.configuration.LastFmProperties;
import com.example.modele.RecommendedTrackDto;
import com.example.modele.SimilarArtistDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class LastFmService {

    private final LastFmProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public LastFmService(LastFmProperties properties) {
        this.properties = properties;
    }

    public List<RecommendedTrackDto> getTopTracksFromLastFm(String artistName, int limit) {
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl())
                .queryParam("method", "artist.gettoptracks")
                .queryParam("artist", artistName)
                .queryParam("api_key", properties.getKey())
                .queryParam("format", "json")
                .queryParam("limit", limit)
                .queryParam("autocorrect", 1)
                .toUriString();

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        List<RecommendedTrackDto> result = new ArrayList<>();

        JsonNode trackNodes = root.path("toptracks").path("track");
        if (trackNodes.isArray()) {
            int rank = 1;
            for (JsonNode node : trackNodes) {
                result.add(new RecommendedTrackDto(
                        node.path("name").asText(),
                        node.path("url").asText(),
                        0,
                        false,
                        rank
                ));
                rank++;
            }
        }

        return result;
    }

    public Integer getTrackRankForArtistSong(String artistName, String songName) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(properties.getBaseUrl())
                    .queryParam("method", "artist.gettoptracks")
                    .queryParam("artist", artistName)
                    .queryParam("api_key", properties.getKey())
                    .queryParam("format", "json")
                    .queryParam("limit", 100)
                    .queryParam("autocorrect", 1)
                    .toUriString();

            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
            JsonNode trackNodes = root.path("toptracks").path("track");

            if (trackNodes.isArray()) {
                int rank = 1;
                String normalizedSongName = simplifyTrackName(songName);

                for (JsonNode node : trackNodes) {
                    String currentTrackName = node.path("name").asText();
                    if (simplifyTrackName(currentTrackName).equals(normalizedSongName)) {
                        return rank;
                    }
                    rank++;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur Last.fm track rank: " + e.getMessage());
        }

        return null;
    }

    public List<SimilarArtistDto> getSimilarArtists(String artistName, int limit) {
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl())
                .queryParam("method", "artist.getsimilar")
                .queryParam("artist", artistName)
                .queryParam("api_key", properties.getKey())
                .queryParam("format", "json")
                .queryParam("limit", limit)
                .queryParam("autocorrect", 1)
                .toUriString();

        JsonNode root = restTemplate.getForObject(url, JsonNode.class);
        List<SimilarArtistDto> result = new ArrayList<>();

        JsonNode artists = root.path("similarartists").path("artist");
        if (artists.isArray()) {
            for (JsonNode artist : artists) {
                result.add(new SimilarArtistDto(
                        artist.path("name").asText(),
                        artist.path("match").asText(),
                        artist.path("url").asText(),
                        extractImage(artist),
                        false,
                        0
                ));
            }
        }

        return result;
    }

    private String extractImage(JsonNode artistNode) {
        JsonNode images = artistNode.path("image");
        if (images.isArray()) {
            for (int i = images.size() - 1; i >= 0; i--) {
                String value = images.get(i).path("#text").asText();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }
        return null;
    }

    private String simplifyTrackName(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\[.*?\\]", "")
                .replaceAll("\\s+-\\s+.*$", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}