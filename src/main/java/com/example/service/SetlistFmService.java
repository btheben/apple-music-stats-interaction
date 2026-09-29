package com.example.service;

import com.example.configuration.SetlistFmProperties;
import com.example.modele.ArtistSetlistDto;
import com.example.modele.SetlistFmArtistDto;
import com.example.modele.SetlistFmArtistSetlistDto;
import com.example.modele.SetlistFmSongStatDto;
import com.example.modele.SongLiveStatsDto;
import com.example.depot.PlayEntryDepot;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SetlistFmService {

    private final SetlistFmProperties properties;
    private final PlayEntryDepot playEntryDepot;
    private final RestTemplate restTemplate = new RestTemplate();

    public SetlistFmService(SetlistFmProperties properties, PlayEntryDepot playEntryDepot) {
        this.properties = properties;
        this.playEntryDepot = playEntryDepot;
    }

    public ArtistSetlistDto getArtistSetlistData(String artistName) {
        ArtistSetlistDto result = new ArtistSetlistDto();
        result.setArtistName(artistName);
        result.setApiEnabled(properties.isEnabled());

        if (!properties.isEnabled() || isBlank(properties.getApiKey())) {
            result.setLatestSetlist(null);
            result.setTopLiveSongs(Collections.emptyList());
            return result;
        }

        SetlistFmArtistDto artist = findBestArtist(artistName);
        if (artist == null || isBlank(artist.getMbid())) {
            result.setLatestSetlist(null);
            result.setTopLiveSongs(Collections.emptyList());
            return result;
        }

        result.setArtistMbid(artist.getMbid());

        List<SetlistFmArtistSetlistDto> setlists = getArtistSetlists(artist.getMbid(), 1, 3);

        SetlistFmArtistSetlistDto latestNonEmptySetlist = setlists.stream()
                .filter(this::hasSongs)
                .findFirst()
                .orElse(null);

        result.setLatestSetlist(latestNonEmptySetlist);

        List<SetlistFmSongStatDto> liveStats = getTopLiveSongsForArtist(artistName, artist.getMbid(), 15);
        result.setTopLiveSongs(liveStats);

        return result;
    }

    public SongLiveStatsDto getSongLiveStats(String artistName, String songName) {
        SongLiveStatsDto dto = new SongLiveStatsDto();
        dto.setArtistName(artistName);
        dto.setSongName(songName);
        dto.setApiEnabled(properties.isEnabled());

        if (!properties.isEnabled() || isBlank(properties.getApiKey())) {
            dto.setPlayedLive(false);
            dto.setBadgeLabel(null);
            return dto;
        }

        SetlistFmArtistDto artist = findBestArtist(artistName);
        if (artist == null || isBlank(artist.getMbid())) {
            dto.setPlayedLive(false);
            dto.setBadgeLabel(null);
            return dto;
        }

        List<SetlistFmArtistSetlistDto> setlists = getArtistSetlists(artist.getMbid(), 1, 10);

        Map<String, Integer> songCounts = new HashMap<>();
        Map<String, String> latestSeenDateBySong = new HashMap<>();

        for (SetlistFmArtistSetlistDto setlist : setlists) {
            for (String liveSong : setlist.getSongs()) {
                String normalized = normalizeSongName(liveSong);
                if (normalized.isBlank()) {
                    continue;
                }

                songCounts.put(normalized, songCounts.getOrDefault(normalized, 0) + 1);

                if (!latestSeenDateBySong.containsKey(normalized)) {
                    latestSeenDateBySong.put(normalized, setlist.getEventDate());
                }
            }
        }

        List<Map.Entry<String, Integer>> ranked = songCounts.entrySet().stream()
                .sorted((a, b) -> {
                    int compare = Integer.compare(b.getValue(), a.getValue());
                    if (compare != 0) {
                        return compare;
                    }
                    return a.getKey().compareTo(b.getKey());
                })
                .collect(Collectors.toList());

        String target = normalizeSongName(songName);
        Integer rank = null;
        Integer timesPlayed = null;

        for (int i = 0; i < ranked.size(); i++) {
            if (isSameSong(target, ranked.get(i).getKey())) {
                rank = i + 1;
                timesPlayed = ranked.get(i).getValue();
                break;
            }
        }

        boolean playedLive = rank != null;

        dto.setPlayedLive(playedLive);
        dto.setLiveRank(rank);
        dto.setTimesPlayedLive(timesPlayed);
        dto.setLastSeenLiveDate(latestSeenDateBySong.get(target));

        if (playedLive) {
            dto.setBadgeLabel("#" + rank + " en spectacle");
        } else {
            dto.setBadgeLabel("Jamais jouée en spectacle");
        }

        return dto;
    }

    public SetlistFmArtistDto findBestArtist(String artistName) {
        if (!properties.isEnabled() || isBlank(properties.getApiKey()) || isBlank(artistName)) {
            return null;
        }

        // 1️⃣ Tentative avec artistName (plus précis)
        JsonNode root = searchArtistByArtistName(artistName);

        // 2️⃣ Fallback avec query si ça fail (très important avec setlist.fm)
        if (root == null || !root.path("artist").isArray() || root.path("artist").isEmpty()) {
            root = searchArtistByQuery(artistName);
        }

        if (root == null) {
            return null;
        }

        JsonNode artists = root.path("artist");
        if (!artists.isArray() || artists.isEmpty()) {
            return null;
        }

        JsonNode best = artists.get(0);

        return new SetlistFmArtistDto(
                best.path("mbid").asText(null),
                best.path("name").asText(null),
                best.path("sortName").asText(null),
                best.path("disambiguation").asText(null),
                best.path("url").asText(null)
        );
    }

    public List<SetlistFmArtistSetlistDto> getArtistSetlists(String artistMbid, int page, int maxPages) {
        List<SetlistFmArtistSetlistDto> result = new ArrayList<>();

        if (!properties.isEnabled() || isBlank(properties.getApiKey()) || isBlank(artistMbid)) {
            return result;
        }

        for (int currentPage = page; currentPage < page + maxPages; currentPage++) {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(properties.getBaseUrl())
                    .path("/artist/" + artistMbid + "/setlists")
                    .queryParam("p", currentPage)
                    .build()
                    .encode()
                    .toUri();

            JsonNode root = executeGet(uri);

            if (root == null) {
                break;
            }

            JsonNode setlists = root.path("setlist");
            if (!setlists.isArray() || setlists.isEmpty()) {
                break;
            }

            for (JsonNode node : setlists) {
                result.add(toSetlistDto(node));
            }

            int total = root.path("total").asInt(0);
            int itemsPerPage = root.path("itemsPerPage").asInt(20);
            int totalPages = itemsPerPage <= 0 ? 1 : (int) Math.ceil(total / (double) itemsPerPage);

            if (currentPage >= totalPages) {
                break;
            }
        }

        return result;
    }

    public List<SetlistFmSongStatDto> getTopLiveSongsForArtist(String artistName, String artistMbid, int limit) {
        List<SetlistFmArtistSetlistDto> setlists = getArtistSetlists(artistMbid, 1, 10);

        Map<String, Integer> songCountMap = new HashMap<>();
        Map<String, String> displayNameMap = new HashMap<>();

        for (SetlistFmArtistSetlistDto setlist : setlists) {
            for (String song : setlist.getSongs()) {
                String normalized = normalizeSongName(song);
                if (normalized.isBlank()) {
                    continue;
                }

                songCountMap.put(normalized, songCountMap.getOrDefault(normalized, 0) + 1);
                displayNameMap.putIfAbsent(normalized, song);
            }
        }

        List<SetlistFmSongStatDto> stats = new ArrayList<>();

        List<Map.Entry<String, Integer>> sorted = songCountMap.entrySet().stream()
                .sorted((a, b) -> {
                    int compare = Integer.compare(b.getValue(), a.getValue());
                    if (compare != 0) {
                        return compare;
                    }
                    return a.getKey().compareTo(b.getKey());
                })
                .limit(limit)
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            String normalizedSong = entry.getKey();
            String displaySong = displayNameMap.getOrDefault(normalizedSong, normalizedSong);

            boolean inLibrary = isSongInLibrary(artistName, displaySong);

            stats.add(new SetlistFmSongStatDto(
                    displaySong,
                    entry.getValue(),
                    rank,
                    inLibrary
            ));

            rank++;
        }

        return stats;
    }

    private boolean isSongInLibrary(String artistName, String songName) {
        List<com.example.modele.AppleMusicEntry> entries =
                playEntryDepot.findByArtistNameIgnoreCaseAndSongNameIgnoreCaseOrderByDatePlayedDesc(artistName, songName);

        if (entries != null && !entries.isEmpty()) {
            return true;
        }

        if (isBlank(artistName) || isBlank(songName)) {
            return false;
        }

        List<PlayEntryDepot.ArtistSongPlayCountProjection> rows =
                playEntryDepot.getSongPlayCountsByArtist(artistName);

        String target = normalizeSongName(songName);

        for (PlayEntryDepot.ArtistSongPlayCountProjection row : rows) {
            if (isSameSong(target, normalizeSongName(row.getSongName()))) {
                return true;
            }
        }

        return false;
    }

    private SetlistFmArtistSetlistDto toSetlistDto(JsonNode node) {
        SetlistFmArtistSetlistDto dto = new SetlistFmArtistSetlistDto();
        dto.setSetlistId(node.path("id").asText(null));
        dto.setVersionId(node.path("versionId").asText(null));
        dto.setEventDate(node.path("eventDate").asText(null));
        dto.setVenueName(node.path("venue").path("name").asText(null));
        dto.setCityName(node.path("venue").path("city").path("name").asText(null));
        dto.setCountryName(node.path("venue").path("city").path("country").path("name").asText(null));
        dto.setTourName(node.path("tour").path("name").asText(null));
        dto.setUrl(node.path("url").asText(null));

        List<String> songs = new ArrayList<>();
        JsonNode sets = node.path("sets").path("set");
        if (sets.isArray()) {
            for (JsonNode set : sets) {
                JsonNode setSongs = set.path("song");
                if (setSongs.isArray()) {
                    for (JsonNode songNode : setSongs) {
                        String songName = songNode.path("name").asText(null);
                        if (!isBlank(songName)) {
                            songs.add(songName);
                        }
                    }
                }
            }
        } else {
            JsonNode altSet = node.path("set");
            if (altSet.isArray()) {
                for (JsonNode set : altSet) {
                    JsonNode setSongs = set.path("song");
                    if (setSongs.isArray()) {
                        for (JsonNode songNode : setSongs) {
                            String songName = songNode.path("name").asText(null);
                            if (!isBlank(songName)) {
                                songs.add(songName);
                            }
                        }
                    }
                }
            }
        }

        dto.setSongs(songs);
        return dto;
    }

    private JsonNode executeGet(URI uri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", properties.getApiKey());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            System.out.println("SETLIST URL = " + uri);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            return response.getBody();

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // 🔥 super important : 404 = aucun résultat, pas une erreur fatale
            System.out.println("SETLIST 404 (no result) for URL: " + uri);
            return null;

        } catch (Exception e) {
            System.out.println("Erreur setlist.fm GET: " + e.getMessage());
            return null;
        }
    }

    private boolean isSameSong(String a, String b) {
        return normalizeSongName(a).equals(normalizeSongName(b));
    }

    private String normalizeSongName(String value) {
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private JsonNode searchArtistByArtistName(String artistName) {
        try {
            var uri = UriComponentsBuilder
                    .fromHttpUrl(properties.getBaseUrl())
                    .path("/search/artists")
                    .queryParam("artistName", artistName)
                    .queryParam("sort", "relevance")
                    .queryParam("p", 1)
                    .build()
                    .encode()
                    .toUri();

            return executeGet(uri);
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode searchArtistByQuery(String artistName) {
        try {
            var uri = UriComponentsBuilder
                    .fromHttpUrl(properties.getBaseUrl())
                    .path("/search/artists")
                    .queryParam("query", artistName)
                    .queryParam("sort", "relevance")
                    .queryParam("p", 1)
                    .build()
                    .encode()
                    .toUri();

            return executeGet(uri);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hasSongs(SetlistFmArtistSetlistDto setlist) {
        return setlist != null
                && setlist.getSongs() != null
                && setlist.getSongs().stream().anyMatch(song -> song != null && !song.trim().isEmpty());
    }
}