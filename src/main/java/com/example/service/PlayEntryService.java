package com.example.service;

import com.example.modele.AppleMusicEntry;
import com.example.depot.ArtistKeysDepot;
import com.example.depot.ExclustionArtistDepot;
import com.example.depot.PlayEntryDepot;
import com.example.depot.TopArtistDepot;
import com.example.modele.*;
import com.example.ressource.SongRessource;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.hc.core5.http.ParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsRelatedArtistsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchArtistsRequest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayEntryService {

    private final PlayEntryDepot playEntryDepot;
    private final ArtistKeysDepot artistKeysDepot;
    private final TopArtistDepot topArtistDepot;
    private final ExclustionArtistDepot exclustionArtistDepot;
    private final LastFmService lastFmService;
    private final ArtistNormalizationRuleService artistNormalizationRuleService;
    private final NormalizationService normalizationService;

    public final String ROOT_FOLDER = "c:\\\\Users\\bensu\\iCloudDrive\\iCloud~is~workflow~my~workflows\\Play Log\\Nouveau";
    public final String ARCHIVE_FOLDER = "c:\\\\Users\\bensu\\iCloudDrive\\iCloud~is~workflow~my~workflows\\Play Log\\Archives";

    private static final String clientId = "2006caa46c2846bda29382306c3d41dc";
    private static final String clientSecret = "746398de26ca411791ba2696d61d5948";
    private static final String code = "";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();

    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

    private static final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();

    public PlayEntryService(PlayEntryDepot playEntryDepot,
                            ArtistKeysDepot artistKeysDepot,
                            TopArtistDepot topArtistDepot,
                            ExclustionArtistDepot exclustionArtistDepot,
                            LastFmService lastFmService,
                            NormalizationService normalizationService,
                            ArtistNormalizationRuleService artistNormalizationRuleService) {
        this.playEntryDepot = playEntryDepot;
        this.artistKeysDepot = artistKeysDepot;
        this.topArtistDepot = topArtistDepot;
        this.exclustionArtistDepot = exclustionArtistDepot;
        this.lastFmService = lastFmService;
        this.normalizationService = normalizationService;
        this.artistNormalizationRuleService = artistNormalizationRuleService;

        generateAccessToken();
    }

    private void generateAccessToken() {
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            System.out.println("Spotify token OK. Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Spotify token ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new RuntimeException("Impossible de générer le token Spotify", e);
        }
    }

    public Artist getByArtist(String artistName) {
        CanonicalArtistResolution resolution =
                artistNormalizationRuleService.resolveCanonicalArtist(artistName);

        String canonicalNormalized = resolution.getCanonicalArtistNameNormalized();
        String canonicalDisplayName = resolution.getCanonicalArtistName();

        Artist artist = new Artist();
        artist.setArtistName(canonicalDisplayName);
        artist.setListRelatedArtists(Collections.emptyList());

        List<AppleMusicEntry> canonicalEntries =
                playEntryDepot.findByArtistNameNormalizedOrderByDatePlayedDesc(canonicalNormalized);

        artist.setListAppleMusicEntry(canonicalEntries);
        artist.setListSongs(playEntryDepot.findTopSongsByArtistNameNormalized(canonicalNormalized));

        if (canonicalEntries != null && !canonicalEntries.isEmpty()) {
            String fallbackDisplayName = playEntryDepot.findDisplayArtistNameByNormalized(canonicalNormalized);
            if (fallbackDisplayName != null && !fallbackDisplayName.isBlank()
                    && (canonicalDisplayName == null || canonicalDisplayName.isBlank())) {
                artist.setArtistName(fallbackDisplayName);
                canonicalDisplayName = fallbackDisplayName;
            }
        }

        artist.setTotalNbPlayed(topArtistDepot.getTotalByArtistNormalized(canonicalNormalized));
        artist.setPosition(topArtistDepot.getPositionByArtistNormalized(canonicalNormalized));

        ArtistKeys keys = artistKeysDepot.findFirstByArtistNameIgnoreCase(canonicalDisplayName);

        try {
            generateAccessToken();
        } catch (Exception e) {
            System.out.println("Spotify token ERROR for [" + canonicalDisplayName + "] : "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        if (keys != null && !isBlank(keys.getSpotifyKey())) {
            artist.setSpotifyKey(keys.getSpotifyKey());
            System.out.println("🔑 Artist key FOUND in DB for [" + canonicalDisplayName + "] : " + keys.getSpotifyKey());
        }

        tryResolveArtistFromSearch(canonicalDisplayName, artist, keys);

        if (!isBlank(artist.getSpotifyKey())) {
            tryLoadRelatedArtists(artist, artist.getSpotifyKey());
        } else {
            System.out.println("⚠️ No Spotify key found for [" + canonicalDisplayName + "]");
        }

        if (isBlank(artist.getImageUrl())) {
            String fallbackImage = resolveArtistImageFallback(canonicalDisplayName, canonicalNormalized, canonicalEntries);

            if (!isBlank(fallbackImage)) {
                artist.setImageUrl(fallbackImage);
                System.out.println("✅ Artist image FALLBACK resolved for [" + canonicalDisplayName + "] : " + fallbackImage);
            } else {
                System.out.println("🚫 No artist image found for [" + canonicalDisplayName + "]");
            }
        }

        return artist;
    }

    private void tryResolveArtistFromSearch(String artistName, Artist artist, ArtistKeys existingKeys) {
        try {
            System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
            SearchArtistsRequest searchArtistsRequest = spotifyApi.searchArtists(artistName)
                    .limit(10)
                    .offset(0)
                    .build();

            Paging<se.michaelthelin.spotify.model_objects.specification.Artist> artistPaging =
                    searchArtistsRequest.execute();

            if (artistPaging == null || artistPaging.getItems() == null) {
                return;
            }

            for (se.michaelthelin.spotify.model_objects.specification.Artist currentArtist : artistPaging.getItems()) {
                if (currentArtist == null || isBlank(currentArtist.getName()) || isBlank(currentArtist.getId())) {
                    continue;
                }

                System.out.println("Spotify candidate artist: " + currentArtist.getName());

                if (!isLikelySameArtist(currentArtist.getName(), artistName)) {
                    continue;
                }

                System.out.println("✅ Spotify artist MATCH for [" + artistName + "] -> " + currentArtist.getName());

                if (isBlank(artist.getSpotifyKey())) {
                    artist.setSpotifyKey(currentArtist.getId());
                    System.out.println("✅ Artist spotifyKey resolved via search for [" + artistName + "] : " + currentArtist.getId());
                }

                if (isBlank(artist.getImageUrl())
                        && currentArtist.getImages() != null
                        && currentArtist.getImages().length > 0) {
                    artist.setImageUrl(currentArtist.getImages()[0].getUrl());
                    System.out.println("✅ Artist image FOUND via searchArtists: " + currentArtist.getImages()[0].getUrl());
                }

                if (existingKeys == null) {
                    cacheArtistKeyIfMissing(artistName, currentArtist.getId());
                }

                break;
            }

        } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
            System.out.println("Spotify searchArtists FORBIDDEN for [" + artistName + "] : " + e.getMessage());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Spotify searchArtists ERROR for [" + artistName + "] : "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void tryLoadRelatedArtists(Artist artist, String spotifyKey) {
        try {
            System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
            GetArtistRequest artistRequest = spotifyApi.getArtist(spotifyKey).build();
            se.michaelthelin.spotify.model_objects.specification.Artist spotifyArtist = artistRequest.execute();

            if (spotifyArtist == null || spotifyArtist.getId() == null) {
                System.out.println("Spotify getArtist introuvable pour key = " + spotifyKey);
                return;
            }

            if (isBlank(artist.getImageUrl())
                    && spotifyArtist.getImages() != null
                    && spotifyArtist.getImages().length > 0) {
                artist.setImageUrl(spotifyArtist.getImages()[0].getUrl());
                System.out.println("✅ Artist image FOUND via getArtist: " + spotifyArtist.getImages()[0].getUrl());
            }

            try {
                GetArtistsRelatedArtistsRequest relatedRequest =
                        spotifyApi.getArtistsRelatedArtists(spotifyArtist.getId()).build();

                se.michaelthelin.spotify.model_objects.specification.Artist[] related = relatedRequest.execute();
                List<Artist> relatedArtists = new ArrayList<>();

                int cpt = 0;
                for (se.michaelthelin.spotify.model_objects.specification.Artist relatedArtist : related) {
                    if (relatedArtist == null || cpt >= 10) {
                        break;
                    }

                    Artist artist1 = new Artist();
                    artist1.setArtistName(relatedArtist.getName());

                    if (relatedArtist.getImages() != null && relatedArtist.getImages().length > 0) {
                        artist1.setImageUrl(relatedArtist.getImages()[0].getUrl());
                    }

                    relatedArtists.add(artist1);
                    cpt++;
                }

                artist.setListRelatedArtists(relatedArtists);
                System.out.println("✅ Related artists FOUND for [" + artist.getArtistName() + "] : " + relatedArtists.size());

            } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
                System.out.println("Spotify related artists FORBIDDEN for key = " + spotifyKey + " : " + e.getMessage());
                artist.setListRelatedArtists(Collections.emptyList());
            }

        } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
            System.out.println("Spotify getArtist FORBIDDEN for key = " + spotifyKey + " : " + e.getMessage());
            artist.setListRelatedArtists(Collections.emptyList());

        } catch (se.michaelthelin.spotify.exceptions.detailed.NotFoundException e) {
            System.out.println("Spotify artist introuvable pour key = " + spotifyKey);
            artist.setListRelatedArtists(Collections.emptyList());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Spotify getArtist ERROR for key = " + spotifyKey + " : "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
            artist.setListRelatedArtists(Collections.emptyList());
        }
    }

    public AppleMusicEntry findEntryById(long id) {
        return playEntryDepot.findById(id).get();
    }

    public void deleteEntry(long id) {
        playEntryDepot.deleteById(id);
    }

    public void updateEntry(AppleMusicEntry entry) {
        playEntryDepot.save(entry);
    }

    public void addExclusionArtist(String artistName) {
        ExclustionArtist exclustionArtist = new ExclustionArtist();
        exclustionArtist.setArtistName(artistName);
        exclustionArtistDepot.save(exclustionArtist);
    }

    public SongRessource getByArtistAndSong(String artistName, String songName) {
        CanonicalArtistResolution resolution =
                artistNormalizationRuleService.resolveCanonicalArtist(artistName);

        String canonicalNormalized = resolution.getCanonicalArtistNameNormalized();
        String canonicalDisplayName = resolution.getCanonicalArtistName();

        List<AppleMusicEntry> listEntries =
                playEntryDepot.findByArtistNameNormalizedAndSongNameOrderByDatePlayedDesc(canonicalNormalized, songName);

        if (listEntries == null || listEntries.isEmpty()) {
            return null;
        }

        AppleMusicEntry firstEntry = listEntries.get(0);
        SongRessource song = new SongRessource(firstEntry);
        song.setArtistName(canonicalDisplayName);

        for (AppleMusicEntry entry : listEntries) {
            if (entry.getImageUrl() != null && !entry.getImageUrl().equalsIgnoreCase("")) {
                song.setImageUrl(entry.getImageUrl());
                break;
            }
        }

        song.setListEntries(listEntries);

        int currentPlayCount = listEntries.stream()
                .map(AppleMusicEntry::getNbPlayed)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        song.setCurrentPlayCount(currentPlayCount);

        Integer lastFmRank = lastFmService.getTrackRankForArtistSong(canonicalDisplayName, songName);
        song.setLastFmRank(lastFmRank);

        SongUpcomingMilestoneProjection milestone = playEntryDepot.getUpcomingMilestoneForSong(canonicalDisplayName, songName);
        if (milestone != null) {
            song.setNextMilestone(milestone.getNextMilestone());
            song.setPlaysNeededForNextMilestone(milestone.getPlaysNeeded());

            if (milestone.getCurrentPlayCount() != null) {
                song.setCurrentPlayCount(milestone.getCurrentPlayCount());
            }

            if (milestone.getCurrentPlayCount() != null
                    && milestone.getNextMilestone() != null
                    && milestone.getNextMilestone() > 0) {
                int progress = (int) Math.floor((milestone.getCurrentPlayCount() * 100.0) / milestone.getNextMilestone());
                song.setMilestoneProgressPercent(Math.min(progress, 100));
            }

            if (milestone.getPlaysNeeded() != null) {
                if (milestone.getPlaysNeeded() <= 3) {
                    song.addHeroBadge("🔥 Ultra proche du prochain milestone", "milestone-hot", "internal");
                } else if (milestone.getPlaysNeeded() <= 5) {
                    song.addHeroBadge("⚡ Très proche du prochain milestone", "milestone-warm", "internal");
                } else if (milestone.getPlaysNeeded() <= 10) {
                    song.addHeroBadge("👀 À surveiller", "milestone-soft", "internal");
                }
            }
        }

        if (lastFmRank != null) {
            song.addHeroBadge("🎵 Top Last.fm #" + lastFmRank, "lastfm", "lastfm");
        }

        return song;
    }

    @Transactional(readOnly = true)
    public List<AppleMusicEntry> getByDatePlayed(Date dateStart, Date dateEnd) {

        List<AppleMusicEntry> entries =
                playEntryDepot.findByDatePlayedBetweenOrderByDatePlayedDesc(
                        dateStart,
                        dateEnd
                );

        if (entries == null || entries.isEmpty()) {
            return entries != null ? entries : Collections.emptyList();
        }

        Date yearStart = getStartOfYear(dateStart);

        List<ArtistYearPlayCountProjection> existingCounts =
                playEntryDepot.getArtistPlayCountsBeforeDay(
                        yearStart,
                        dateStart
                );

        Map<String, Long> runningCounts = new HashMap<>();

        if (existingCounts != null) {
            for (ArtistYearPlayCountProjection projection : existingCounts) {
                if (projection == null || projection.getArtistKey() == null) {
                    continue;
                }

                long playCount = projection.getPlayCount() != null
                        ? projection.getPlayCount()
                        : 0L;

                runningCounts.put(
                        projection.getArtistKey().trim().toLowerCase(Locale.ROOT),
                        playCount
                );
            }
        }

        /*
         * La liste retournée par le repository est en ordre décroissant.
         * Pour attribuer 12e, 13e, 14e correctement, on travaille temporairement
         * dans l'ordre chronologique.
         */
        List<AppleMusicEntry> chronologicalEntries = new ArrayList<>(entries);

        chronologicalEntries.sort(
                Comparator
                        .comparing(
                                AppleMusicEntry::getDatePlayed,
                                Comparator.nullsLast(Date::compareTo)
                        )
                        .thenComparing(
                                AppleMusicEntry::getId,
                                Comparator.nullsLast(Long::compareTo)
                        )
        );

        for (AppleMusicEntry entry : chronologicalEntries) {
            String artistKey = getArtistCounterKey(entry);

            if (artistKey == null || artistKey.isBlank()) {
                entry.setArtistYearPlayNumber(null);
                continue;
            }

            long nextPlayNumber =
                    runningCounts.getOrDefault(artistKey, 0L) + 1L;

            entry.setArtistYearPlayNumber(nextPlayNumber);
            runningCounts.put(artistKey, nextPlayNumber);
        }

        /*
         * On retourne la liste originale, qui demeure triée de la plus récente
         * à la plus ancienne comme avant.
         */
        return entries;
    }

    private Date getStartOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private String getArtistCounterKey(AppleMusicEntry entry) {
        if (entry == null) {
            return null;
        }

        String artistNameNormalized = entry.getArtistNameNormalized();

        if (artistNameNormalized != null
                && !artistNameNormalized.trim().isEmpty()) {
            return artistNameNormalized
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        String artistName = entry.getArtistName();

        if (artistName == null || artistName.trim().isEmpty()) {
            return null;
        }

        return artistName
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public List<TopArtist> getTopArtistDateInterval(Date dateStart, Date dateEnd) {
        return playEntryDepot.getTopArtistDateInterval(dateStart, dateEnd);
    }

    public List<TopArtist> getTopArtistAllTime() {
        return topArtistDepot.getTopArtistAllTimeNormalized();
    }

    public List<TopSong> getTopSongDateInterval(Date dateStart, Date dateEnd) {
        return playEntryDepot.getTopSongDateInterval(dateStart, dateEnd);
    }

    public List<TopAlbum> getTopAlbumDateInterval(Date dateStart, Date dateEnd) {
        return playEntryDepot.getTopAlbumDateInterval(dateStart, dateEnd);
    }

    public List<TopArtist> getTopArtistLastWeek(Date dateEnd) {
        return playEntryDepot.getTopArtistsLastWeek(dateEnd);
    }

    public List<TopSong> getTopSongLastWeek(Date dateEnd) {
        return playEntryDepot.getTopSongLastWeek(dateEnd);
    }

    public int getNbFiles() {
        File folderNewFiles = new File(ROOT_FOLDER);
        File[] listOfFiles = folderNewFiles.listFiles();

        if (listOfFiles == null) {
            return 0;
        }

        return (int) java.util.Arrays.stream(listOfFiles)
                .filter(File::isFile)
                .count();
    }

    public void loadFiles() {
        File folderNewFiles = new File(ROOT_FOLDER);
        File[] listOfFiles = folderNewFiles.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("Aucun fichier à importer.");
            return;
        }

        Path tempProcessingDir = Paths.get("C:\\tmp\\playlog-processing");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US);

        try {
            Files.createDirectories(tempProcessingDir);
            Files.createDirectories(Paths.get(ARCHIVE_FOLDER));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer les dossiers temporaires/archives", e);
        }

        generateAccessToken();

        int totalImportedEntries = 0;

        for (File file : listOfFiles) {
            if (file == null || !file.isFile()) {
                continue;
            }

            Path sourcePath = file.toPath();
            Path tempPath = tempProcessingDir.resolve(file.getName());
            Path archivePath = Paths.get(ARCHIVE_FOLDER).resolve(file.getName());

            System.out.println("\n==================================================");
            System.out.println("Traitement du fichier : " + sourcePath);
            System.out.println("==================================================");

            try {
                waitForFileToBeStable(sourcePath, 3, 1000);

                copyWithRetry(sourcePath, tempPath, 5, 500);
                System.out.println("Copie temporaire OK : " + tempPath);

                int importedForCurrentFile = importCsvFile(tempPath, dateFormat);
                totalImportedEntries += importedForCurrentFile;

                System.out.println("Import OK pour [" + file.getName() + "] : " + importedForCurrentFile + " entrées");

                moveWithRetry(sourcePath, archivePath, 10, 750);
                System.out.println("Archive OK : " + archivePath);

                deleteTempFileQuietly(tempPath);

            } catch (Exception e) {
                System.out.println("ERREUR traitement fichier [" + file.getName() + "] : "
                        + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();

                deleteTempFileQuietly(tempPath);
            }
        }

        System.out.println("\nTotal des entrées importées : " + totalImportedEntries);
    }

    private int importCsvFile(Path csvPath, SimpleDateFormat dateFormat) throws Exception {
        int importedCount = 0;
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(csvPath, StandardCharsets.UTF_8))
                .withCSVParser(csvParser)
                .build()) {

            List<String[]> rows = reader.readAll();

            for (String[] entry : rows) {
                if (entry == null || entry.length < 9) {
                    System.out.println("Ligne ignorée (format invalide) dans " + csvPath.getFileName());
                    continue;
                }

                AppleMusicEntry appleMusicEntry = new AppleMusicEntry();
                appleMusicEntry.setArtistName(entry[0]);
                appleMusicEntry.setArtistNameNormalized(
                        artistNormalizationRuleService.resolveCanonicalArtistNormalized(entry[0])
                );
                appleMusicEntry.setAlbumName(entry[1]);
                appleMusicEntry.setNbPlayed(Integer.valueOf(entry[2]));
                appleMusicEntry.setDatePlayed(dateFormat.parse(entry[3]));
                appleMusicEntry.setLength(entry[4]);
                appleMusicEntry.setGenre(entry[5]);
                appleMusicEntry.setRating(Integer.valueOf(entry[6]));
                appleMusicEntry.setDateAdded(dateFormat.parse(entry[7]));
                appleMusicEntry.setSongName(entry[8]);

                getAlbumArtwork(appleMusicEntry);

                System.out.println(appleMusicEntry.toString());
                playEntryDepot.save(appleMusicEntry);
                importedCount++;
            }
        }

        return importedCount;
    }

    private void copyWithRetry(Path source, Path target, int maxAttempts, long delayMs)
            throws IOException, InterruptedException {

        IOException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Files.createDirectories(target.getParent());
                Files.copy(source, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
                return;
            } catch (IOException e) {
                lastException = e;
                System.out.println("Copy failed attempt " + attempt + "/" + maxAttempts
                        + " for " + source.getFileName() + " : " + e.getMessage());
                Thread.sleep(delayMs);
            }
        }

        throw lastException;
    }

    private void moveWithRetry(Path source, Path target, int maxAttempts, long delayMs)
            throws IOException, InterruptedException {

        IOException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Files.createDirectories(target.getParent());
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException e) {
                lastException = e;
                System.out.println("Move failed attempt " + attempt + "/" + maxAttempts
                        + " for " + source.getFileName() + " : " + e.getMessage());
                Thread.sleep(delayMs);
            }
        }

        throw lastException;
    }

    private void waitForFileToBeStable(Path path, int checks, long delayMs)
            throws IOException, InterruptedException {

        long previousSize = -1L;
        long previousModified = -1L;
        int stableCount = 0;

        while (stableCount < checks) {
            if (!Files.exists(path)) {
                throw new IOException("Le fichier n'existe plus : " + path);
            }

            long currentSize = Files.size(path);
            long currentModified = Files.getLastModifiedTime(path).toMillis();

            if (currentSize == previousSize && currentModified == previousModified) {
                stableCount++;
            } else {
                stableCount = 0;
            }

            previousSize = currentSize;
            previousModified = currentModified;

            Thread.sleep(delayMs);
        }
    }

    private void deleteTempFileQuietly(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
        } catch (IOException e) {
            System.out.println("Impossible de supprimer le fichier temporaire : "
                    + tempPath + " - " + e.getMessage());
        }
    }

    public String getAlbumArtwork(AppleMusicEntry appleMusicEntry) {
        if (appleMusicEntry == null
                || isBlank(appleMusicEntry.getArtistName())
                || isBlank(appleMusicEntry.getAlbumName())) {
            System.out.println("⚠️ Artwork SKIP (données invalides)");
            return "";
        }

        String artistName = appleMusicEntry.getArtistName().trim();
        String albumName = appleMusicEntry.getAlbumName().trim();

        System.out.println("\n🎵 Artwork START → [" + artistName + "] / [" + albumName + "]");

        try {
            generateAccessToken();

            ArtistKeys keys = artistKeysDepot.findFirstByArtistNameIgnoreCase(artistName);
            if (keys != null && !isBlank(keys.getSpotifyKey())) {
                System.out.println("🔑 Strategy 0: DB artist key → " + keys.getSpotifyKey());

                try {
                    String imageUrl = findAlbumImageFromArtistAlbums(keys.getSpotifyKey(), albumName);
                    if (!isBlank(imageUrl)) {
                        System.out.println("✅ FOUND via DB artist key → " + imageUrl);
                        appleMusicEntry.setImageUrl(imageUrl);
                        return imageUrl;
                    } else {
                        System.out.println("❌ Not found via DB artist key");
                    }
                } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
                    System.out.println("⚠️ Spotify FORBIDDEN via DB key for [" + artistName + "] : " + e.getMessage());
                }
            }

            System.out.println("🔍 Strategy 1: Search Artist → Albums");

            String imageUrl = tryFindArtworkFromArtistAlbums(artistName, albumName);

            if (!isBlank(imageUrl)) {
                System.out.println("✅ FOUND via ArtistAlbums → " + imageUrl);
                appleMusicEntry.setImageUrl(imageUrl);
                return imageUrl;
            } else {
                System.out.println("❌ Not found via ArtistAlbums");
            }

            System.out.println("🔍 Strategy 2: Search Album");

            imageUrl = tryFindArtworkByAlbumSearch(artistName, albumName);

            if (!isBlank(imageUrl)) {
                System.out.println("✅ FOUND via Search → " + imageUrl);
                appleMusicEntry.setImageUrl(imageUrl);
                return imageUrl;
            } else {
                System.out.println("❌ Not found via Search");
            }

        } catch (Exception e) {
            System.out.println("💥 ERROR Spotify → " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }

        System.out.println("🚫 FINAL RESULT → NO ARTWORK FOUND [" + artistName + "] / [" + albumName + "]");
        return "";
    }

    private String tryFindArtworkFromArtistAlbums(String artistName, String albumName)
            throws IOException, SpotifyWebApiException, ParseException {

        System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
        SearchArtistsRequest searchArtistsRequest = spotifyApi.searchArtists(artistName)
                .limit(10)
                .offset(0)
                .build();

        final Paging<se.michaelthelin.spotify.model_objects.specification.Artist> artistPaging =
                searchArtistsRequest.execute();

        if (artistPaging == null || artistPaging.getItems() == null) {
            return "";
        }

        for (se.michaelthelin.spotify.model_objects.specification.Artist artist : artistPaging.getItems()) {
            if (artist == null || isBlank(artist.getName()) || isBlank(artist.getId())) {
                continue;
            }

            if (!isLikelySameArtist(artist.getName(), artistName)) {
                continue;
            }

            try {
                String imageUrl = findAlbumImageFromArtistAlbums(artist.getId(), albumName);
                if (!isBlank(imageUrl)) {
                    cacheArtistKeyIfMissing(artistName, artist.getId());
                    return imageUrl;
                }
            } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
                System.out.println("Spotify Forbidden sur getArtistsAlbums/getAlbum pour artiste [" + artistName + "] : "
                        + e.getMessage());
                return "";
            }
        }

        return "";
    }

    private String findAlbumImageFromArtistAlbums(String artistId, String albumName)
            throws IOException, SpotifyWebApiException, ParseException {

        int offset = 0;
        int limit = 50;

        while (true) {
            System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
            GetArtistsAlbumsRequest artistsAlbumsRequest = spotifyApi.getArtistsAlbums(artistId)
                    .limit(limit)
                    .offset(offset)
                    .build();

            Paging<AlbumSimplified> albumList = artistsAlbumsRequest.execute();

            if (albumList == null || albumList.getItems() == null || albumList.getItems().length == 0) {
                break;
            }

            for (AlbumSimplified albumSimplified : albumList.getItems()) {
                if (albumSimplified == null || isBlank(albumSimplified.getName()) || isBlank(albumSimplified.getId())) {
                    continue;
                }

                if (!isLikelySameAlbum(albumSimplified.getName(), albumName)) {
                    continue;
                }

                try {
                    System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
                    GetAlbumRequest albumRequest = spotifyApi.getAlbum(albumSimplified.getId()).build();
                    Album album = albumRequest.execute();

                    if (album != null && album.getImages() != null && album.getImages().length > 0) {
                        return album.getImages()[0].getUrl();
                    }
                } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
                    System.out.println("Spotify Forbidden sur getAlbum [" + albumSimplified.getName() + "] : "
                            + e.getMessage());
                    return "";
                }
            }

            offset += limit;
            if (offset >= albumList.getTotal()) {
                break;
            }
        }

        return "";
    }

    private String tryFindArtworkByAlbumSearch(String artistName, String albumName)
            throws IOException, SpotifyWebApiException, ParseException {

        System.out.println("🎵 Album raw = [" + albumName + "]");
        System.out.println("🎵 Album normalized = [" + normalizeAlbumSearchKey(albumName) + "]");

        List<String> queries = new ArrayList<>();
        queries.add(artistName + " " + albumName);

        String normalizedAlbum = normalizeAlbumSearchKey(albumName);
        if (!normalizedAlbum.isBlank() && !normalizedAlbum.equalsIgnoreCase(albumName.trim())) {
            queries.add(artistName + " " + normalizedAlbum);
        }

        for (String query : queries) {
            System.out.println("🔎 Album search query = [" + query + "]");

            System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
            SearchAlbumsRequest searchAlbumsRequest =
                    spotifyApi.searchAlbums(query)
                            .limit(10)
                            .offset(0)
                            .build();

            Paging<AlbumSimplified> albumPaging = searchAlbumsRequest.execute();

            if (albumPaging == null || albumPaging.getItems() == null) {
                continue;
            }

            AlbumSimplified bestMatch = null;
            int bestScore = -1;

            for (AlbumSimplified candidate : albumPaging.getItems()) {
                if (candidate == null || isBlank(candidate.getName())) {
                    continue;
                }

                int score = computeAlbumMatchScore(candidate, artistName, albumName);
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = candidate;
                }
            }

            if (bestMatch != null && bestScore >= 40) {
                if (bestMatch.getImages() != null && bestMatch.getImages().length > 0) {
                    return bestMatch.getImages()[0].getUrl();
                }

                if (!isBlank(bestMatch.getId())) {
                    try {
                        System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
                        GetAlbumRequest albumRequest = spotifyApi.getAlbum(bestMatch.getId()).build();
                        Album album = albumRequest.execute();
                        if (album != null && album.getImages() != null && album.getImages().length > 0) {
                            return album.getImages()[0].getUrl();
                        }
                    } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
                        System.out.println("Spotify Forbidden sur getAlbum fallback [" + bestMatch.getName() + "] : "
                                + e.getMessage());
                    }
                }
            }
        }

        return "";
    }

    private int computeAlbumMatchScore(AlbumSimplified candidate, String artistName, String albumName) {
        int score = 0;

        String normalizedCandidateAlbum = normalize(candidate.getName());
        String normalizedTargetAlbum = normalize(albumName);

        if (normalizedCandidateAlbum.equals(normalizedTargetAlbum)) {
            score += 70;
        } else if (normalizedCandidateAlbum.contains(normalizedTargetAlbum)
                || normalizedTargetAlbum.contains(normalizedCandidateAlbum)) {
            score += 45;
        }

        String normalizedCandidateAlbumSearch = normalizeAlbumSearchKey(candidate.getName());
        String normalizedTargetAlbumSearch = normalizeAlbumSearchKey(albumName);

        if (normalizedCandidateAlbumSearch.equals(normalizedTargetAlbumSearch)) {
            score += 40;
        } else if (normalizedCandidateAlbumSearch.contains(normalizedTargetAlbumSearch)
                || normalizedTargetAlbumSearch.contains(normalizedCandidateAlbumSearch)) {
            score += 20;
        }

        if (candidate.getArtists() != null) {
            for (ArtistSimplified artist : candidate.getArtists()) {
                if (artist != null && !isBlank(artist.getName())) {
                    String normalizedCandidateArtist = normalize(artist.getName());
                    String normalizedTargetArtist = normalize(artistName);

                    if (normalizedCandidateArtist.equals(normalizedTargetArtist)) {
                        score += 30;
                        break;
                    } else if (normalizedCandidateArtist.contains(normalizedTargetArtist)
                            || normalizedTargetArtist.contains(normalizedCandidateArtist)) {
                        score += 15;
                        break;
                    }
                }
            }
        }

        return score;
    }

    private String findArtistIdByExactName(String artistName)
            throws IOException, SpotifyWebApiException, ParseException {

        System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
        SearchArtistsRequest searchArtistsRequest = spotifyApi.searchArtists(artistName)
                .limit(10)
                .offset(0)
                .build();

        Paging<se.michaelthelin.spotify.model_objects.specification.Artist> artistPaging =
                searchArtistsRequest.execute();

        if (artistPaging == null || artistPaging.getItems() == null) {
            return null;
        }

        for (se.michaelthelin.spotify.model_objects.specification.Artist artist : artistPaging.getItems()) {
            if (artist != null && isLikelySameArtist(artist.getName(), artistName) && !isBlank(artist.getId())) {
                return artist.getId();
            }
        }

        return null;
    }

    private void cacheArtistKeyIfMissing(String artistName, String spotifyArtistId) {
        if (isBlank(artistName) || isBlank(spotifyArtistId)) {
            return;
        }

        ArtistKeys artistKeys = artistKeysDepot.findFirstByArtistNameIgnoreCase(artistName);

        if (artistKeys == null) {
            artistKeys = new ArtistKeys();
            artistKeys.setArtistName(artistName);
            artistKeys.setSpotifyKey(spotifyArtistId);
            artistKeysDepot.save(artistKeys);
            System.out.println("✅ Artist key cached for [" + artistName + "] : " + spotifyArtistId);
        }
    }

    private void upsertArtistKey(String artistName, String spotifyArtistId) {
        if (isBlank(artistName) || isBlank(spotifyArtistId)) {
            return;
        }

        ArtistKeys artistKeys = artistKeysDepot.findFirstByArtistNameIgnoreCase(artistName);

        if (artistKeys == null) {
            artistKeys = new ArtistKeys();
            artistKeys.setArtistName(artistName);
            artistKeys.setSpotifyKey(spotifyArtistId);
            artistKeysDepot.save(artistKeys);
            return;
        }

        if (isBlank(artistKeys.getSpotifyKey())) {
            artistKeys.setSpotifyKey(spotifyArtistId);
            artistKeysDepot.save(artistKeys);
        }
    }

    private boolean isLikelySameArtist(String spotifyArtistName, String localArtistName) {
        String a = normalize(spotifyArtistName);
        String b = normalize(localArtistName);

        return a.equals(b) || a.contains(b) || b.contains(a);
    }

    private boolean isLikelySameAlbum(String spotifyAlbumName, String localAlbumName) {
        String rawA = normalize(spotifyAlbumName);
        String rawB = normalize(localAlbumName);

        if (rawA.equals(rawB) || rawA.contains(rawB) || rawB.contains(rawA)) {
            return true;
        }

        String a = normalizeAlbumSearchKey(spotifyAlbumName);
        String b = normalizeAlbumSearchKey(localAlbumName);

        return a.equals(b) || a.contains(b) || b.contains(a);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace("’", "'")
                .replace("`", "'")
                .replace("‐", "-")
                .replace("–", "-")
                .replace("—", "-")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public List<DailyPlayCountProjection> getDailyPlayCountBetween(Date dateStart, Date dateEnd) {
        return playEntryDepot.getDailyPlayCountBetween(dateStart, dateEnd);
    }

    public List<PeriodPlayCountProjection> getTop10Days(Date dateStart, Date dateEnd) {
        return playEntryDepot.getTop10Days(dateStart, dateEnd);
    }

    public List<PeriodPlayCountProjection> getTop10Months(Date dateStart, Date dateEnd) {
        return playEntryDepot.getTop10Months(dateStart, dateEnd);
    }

    public List<PeriodPlayCountProjection> getTop10Years(Date dateStart, Date dateEnd) {
        return playEntryDepot.getTop10Years(dateStart, dateEnd);
    }

    public List<NewArtistProjection> getTopNewArtists(Date dateStart, Date dateEnd, Integer limitValue) {
        return playEntryDepot.getTopNewArtists(dateStart, dateEnd, limitValue);
    }

    public List<RediscoveryArtistProjection> getRandomRediscoveryArtists(Integer minPlays, Integer limitValue) {
        return playEntryDepot.getRandomRediscoveryArtists(minPlays, limitValue);
    }

    public List<PeriodPlayCountProjection> getMonthlyPlayCountBetween(Date dateStart, Date dateEnd) {
        return playEntryDepot.getMonthlyPlayCountBetween(dateStart, dateEnd);
    }

    public List<PeriodPlayCountProjection> getYearlyPlayCountBetween(Date dateStart, Date dateEnd) {
        return playEntryDepot.getYearlyPlayCountBetween(dateStart, dateEnd);
    }

    public List<HIndexArtistProjection> getArtistHIndexDetails(Date dateStart, Date dateEnd) {
        return playEntryDepot.getArtistHIndexDetails(dateStart, dateEnd);
    }

    public HIndexSummaryProjection getArtistHIndexSummary(Date dateStart, Date dateEnd) {
        return playEntryDepot.getArtistHIndexSummary(dateStart, dateEnd);
    }

    public List<RecentRediscoveryProjection> getRecentRediscoveries(Date dateStart,
                                                                    Date dateEnd,
                                                                    Integer minGapDays,
                                                                    Integer minTotalPlays,
                                                                    Integer limitValue) {
        return playEntryDepot.getRecentRediscoveries(dateStart, dateEnd, minGapDays, minTotalPlays, limitValue);
    }

    public Long getNewArtistCount(Date dateStart, Date dateEnd) {
        NewArtistCountProjection projection = playEntryDepot.getNewArtistCount(dateStart, dateEnd);

        if (projection == null || projection.getArtistCount() == null) {
            return 0L;
        }

        return projection.getArtistCount();
    }

    @Transactional
    public int updateSongMetadata(String artistName,
                                  String oldSongName,
                                  String newSongName,
                                  String newAlbumName) {
        String safeArtistName = artistName == null ? "" : artistName.trim();
        String safeOldSongName = oldSongName == null ? "" : oldSongName.trim();
        String safeNewSongName = newSongName == null ? "" : newSongName.trim();
        String safeNewAlbumName = newAlbumName == null ? "" : newAlbumName.trim();

        if (safeArtistName.isEmpty()) {
            throw new IllegalArgumentException("artistName ne peut pas être vide");
        }

        if (safeOldSongName.isEmpty()) {
            throw new IllegalArgumentException("oldSongName ne peut pas être vide");
        }

        if (safeNewSongName.isEmpty()) {
            throw new IllegalArgumentException("newSongName ne peut pas être vide");
        }

        if (safeNewAlbumName.isEmpty()) {
            throw new IllegalArgumentException("newAlbumName ne peut pas être vide");
        }

        return playEntryDepot.updateSongMetadata(
                safeArtistName,
                safeOldSongName,
                safeNewSongName,
                safeNewAlbumName
        );
    }

    public List<TopSong> getTopSongDateInterval(Date dateStart, Date dateEnd, int limit) {
        return playEntryDepot.getTopSongDateInterval(dateStart, dateEnd, PageRequest.of(0, limit));
    }

    public List<TopAlbum> getTopAlbumDateInterval(Date dateStart, Date dateEnd, Integer limit) {
        return playEntryDepot.getTopAlbumDateInterval(dateStart, dateEnd, PageRequest.of(0, limit));
    }

    public List<TopArtist> getTopArtistDateInterval(Date dateStart, Date dateEnd, Integer limit) {
        return playEntryDepot.getTopArtistDateInterval(dateStart, dateEnd, PageRequest.of(0, limit));
    }

    public List<RecentMilestoneProjection> getRecentMilestones(Date dateStart, Date dateEnd) {
        return playEntryDepot.getRecentMilestones(dateStart, dateEnd);
    }

    public List<UpcomingMilestoneProjection> getUpcomingMilestones(Date recentDateStart,
                                                                   Date dateEnd,
                                                                   Integer maxPlaysNeeded,
                                                                   Integer minPercent,
                                                                   Integer limitValue) {
        return playEntryDepot.getUpcomingMilestones(
                recentDateStart,
                dateEnd,
                maxPlaysNeeded,
                minPercent,
                limitValue
        );
    }

    private String resolveArtistImageFallback(String artistDisplayName,
                                              String artistNameNormalized,
                                              List<AppleMusicEntry> localEntries) {
        String localEntryImage = findImageFromLocalEntries(localEntries);
        if (!isBlank(localEntryImage)) {
            System.out.println("🟡 Artist fallback via local entries for [" + artistDisplayName + "]");
            return localEntryImage;
        }

        String topAlbumImage = findImageFromTopAlbum(artistNameNormalized);
        if (!isBlank(topAlbumImage)) {
            System.out.println("🟡 Artist fallback via top album for [" + artistDisplayName + "]");
            return topAlbumImage;
        }

        try {
            generateAccessToken();
            String spotifyAlbumImage = findArtistImageViaAlbumSearch(artistDisplayName);
            if (!isBlank(spotifyAlbumImage)) {
                System.out.println("🟡 Artist fallback via Spotify album search for [" + artistDisplayName + "]");
                return spotifyAlbumImage;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Artist fallback Spotify album search ERROR for [" + artistDisplayName + "] : "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        return "";
    }

    private String findImageFromLocalEntries(List<AppleMusicEntry> localEntries) {
        if (localEntries == null || localEntries.isEmpty()) {
            return "";
        }

        return localEntries.stream()
                .map(AppleMusicEntry::getImageUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .findFirst()
                .orElse("");
    }

    private String findImageFromTopAlbum(String artistNameNormalized) {
        if (isBlank(artistNameNormalized)) {
            return "";
        }

        List<AppleMusicEntry> entries = playEntryDepot.findByArtistNameNormalizedOrderByDatePlayedDesc(artistNameNormalized);

        if (entries == null || entries.isEmpty()) {
            return "";
        }

        Map<String, Long> albumCounts = entries.stream()
                .filter(e -> !isBlank(e.getAlbumName()))
                .collect(Collectors.groupingBy(
                        e -> normalize(e.getAlbumName()),
                        Collectors.counting()
                ));

        Optional<String> topAlbumNormalized = albumCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst();

        if (topAlbumNormalized.isEmpty()) {
            return "";
        }

        String targetAlbum = topAlbumNormalized.get();

        return entries.stream()
                .filter(e -> !isBlank(e.getAlbumName()))
                .filter(e -> normalize(e.getAlbumName()).equals(targetAlbum))
                .map(AppleMusicEntry::getImageUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .findFirst()
                .orElse("");
    }

    private String findArtistImageViaAlbumSearch(String artistName)
            throws IOException, SpotifyWebApiException, ParseException {

        System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
        SearchAlbumsRequest searchAlbumsRequest = spotifyApi.searchAlbums(artistName)
                .limit(10)
                .offset(0)
                .build();

        Paging<AlbumSimplified> albumPaging = searchAlbumsRequest.execute();

        if (albumPaging == null || albumPaging.getItems() == null) {
            return "";
        }

        AlbumSimplified bestMatch = null;
        int bestScore = -1;

        for (AlbumSimplified candidate : albumPaging.getItems()) {
            if (candidate == null) {
                continue;
            }

            int score = computeArtistAlbumFallbackScore(candidate, artistName);
            System.out.println("   ↳ Artist fallback candidate album: "
                    + candidate.getName() + " | score=" + score);

            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        if (bestMatch == null || bestScore < 20) {
            return "";
        }

        if (bestMatch.getImages() != null && bestMatch.getImages().length > 0) {
            return bestMatch.getImages()[0].getUrl();
        }

        if (!isBlank(bestMatch.getId())) {
            try {
                System.out.println("🎯 TOKEN = " + spotifyApi.getAccessToken());
                GetAlbumRequest albumRequest = spotifyApi.getAlbum(bestMatch.getId()).build();
                Album album = albumRequest.execute();

                if (album != null && album.getImages() != null && album.getImages().length > 0) {
                    return album.getImages()[0].getUrl();
                }
            } catch (se.michaelthelin.spotify.exceptions.detailed.ForbiddenException e) {
                System.out.println("⚠️ Spotify getAlbum FORBIDDEN in artist fallback for [" + artistName + "]");
            }
        }

        return "";
    }

    private int computeArtistAlbumFallbackScore(AlbumSimplified candidate, String artistName) {
        int score = 0;

        if (candidate.getArtists() != null) {
            for (ArtistSimplified artist : candidate.getArtists()) {
                if (artist == null || isBlank(artist.getName())) {
                    continue;
                }

                String a = normalize(artist.getName());
                String b = normalize(artistName);

                if (a.equals(b)) {
                    score += 50;
                    break;
                } else if (a.contains(b) || b.contains(a)) {
                    score += 25;
                    break;
                }
            }
        }

        if (!isBlank(candidate.getName())) {
            String albumName = normalize(candidate.getName());

            if (albumName.contains("greatest hits")
                    || albumName.contains("best of")
                    || albumName.contains("essentials")) {
                score += 10;
            }
        }

        return score;
    }

    public DailySummaryDto getDailySummary(Date dateStart, Date dateEnd) {
        List<AppleMusicEntry> entries = getByDatePlayed(dateStart, dateEnd);

        DailySummaryDto dto = new DailySummaryDto();
        dto.setTotalEntries(entries.size());

        dto.setDistinctArtists((int) entries.stream()
                .map(AppleMusicEntry::getArtistNameNormalized)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .count());

        dto.setDistinctSongs((int) entries.stream()
                .filter(e -> e.getArtistNameNormalized() != null && e.getSongName() != null)
                .map(e -> e.getArtistNameNormalized().trim() + "||" + normalize(e.getSongName()))
                .distinct()
                .count());

        dto.setTopSong(resolveTopSong(entries));
        dto.setNewestArtist(resolveNewestArtist(entries, dateStart, dateEnd));
        dto.setRediscoveryArtist(resolveRediscovery(entries, dateStart));

        return dto;
    }

    private DailySummaryDto.SummaryEntryDto resolveTopSong(List<AppleMusicEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        AppleMusicEntry topEntry = entries.stream()
                .filter(Objects::nonNull)
                .max(Comparator
                        .comparing(AppleMusicEntry::getNbPlayed)
                        .thenComparing(e -> e.getDatePlayed() == null ? new Date(0) : e.getDatePlayed()))
                .orElse(null);

        if (topEntry == null) {
            return null;
        }

        DailySummaryDto.SummaryEntryDto dto = toSummaryEntry(topEntry);
        dto.setLabel("Chanson avec le nbPlayed le plus élevé");
        return dto;
    }

    private DailySummaryDto.SummaryEntryDto resolveNewestArtist(List<AppleMusicEntry> entries, Date dateStart, Date dateEnd) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        List<NewArtistProjection> newArtists = getTopNewArtists(dateStart, dateEnd, 100);

        if (newArtists == null || newArtists.isEmpty()) {
            return null;
        }

        Set<String> newArtistNames = newArtists.stream()
                .map(NewArtistProjection::getArtistName)
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.toSet());

        AppleMusicEntry newestEntry = entries.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getArtistName() != null)
                .filter(e -> newArtistNames.contains(normalize(e.getArtistName())))
                .max(Comparator.comparing(e -> e.getDatePlayed() == null ? new Date(0) : e.getDatePlayed()))
                .orElse(null);

        if (newestEntry == null) {
            return null;
        }

        DailySummaryDto.SummaryEntryDto dto = toSummaryEntry(newestEntry);
        dto.setLabel("Dernier nouvel artiste de la journée");
        return dto;
    }

    private DailySummaryDto.SummaryEntryDto resolveRediscovery(List<AppleMusicEntry> entries, Date dateStart) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        Map<String, AppleMusicEntry> firstEntryByArtist = new LinkedHashMap<>();

        for (AppleMusicEntry entry : entries.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(e -> e.getDatePlayed() == null ? new Date(0) : e.getDatePlayed()))
                .collect(Collectors.toList())) {

            if (entry.getArtistNameNormalized() == null || entry.getArtistNameNormalized().trim().isEmpty()) {
                continue;
            }

            String normalizedArtist = entry.getArtistNameNormalized().trim();
            firstEntryByArtist.putIfAbsent(normalizedArtist, entry);
        }

        AppleMusicEntry bestEntry = null;
        long bestGapDays = -1;

        for (Map.Entry<String, AppleMusicEntry> candidateEntry : firstEntryByArtist.entrySet()) {
            String artistNormalized = candidateEntry.getKey();
            AppleMusicEntry candidate = candidateEntry.getValue();

            List<AppleMusicEntry> artistHistory =
                    playEntryDepot.findByArtistNameNormalizedOrderByDatePlayedDesc(artistNormalized);

            Date previousPlay = null;

            for (AppleMusicEntry historicalEntry : artistHistory) {
                if (historicalEntry.getDatePlayed() != null && historicalEntry.getDatePlayed().before(dateStart)) {
                    previousPlay = historicalEntry.getDatePlayed();
                    break;
                }
            }

            if (previousPlay == null) {
                continue;
            }

            long diffMillis = dateStart.getTime() - previousPlay.getTime();
            long gapDays = diffMillis / (1000L * 60L * 60L * 24L);

            if (gapDays > bestGapDays) {
                bestGapDays = gapDays;
                bestEntry = candidate;
            }
        }

        if (bestEntry == null) {
            return null;
        }

        DailySummaryDto.SummaryEntryDto dto = toSummaryEntry(bestEntry);
        dto.setGapDays((int) bestGapDays);
        dto.setLabel("Artiste resté silencieux le plus longtemps avant aujourd’hui");
        return dto;
    }

    private DailySummaryDto.SummaryEntryDto toSummaryEntry(AppleMusicEntry entry) {
        DailySummaryDto.SummaryEntryDto dto = new DailySummaryDto.SummaryEntryDto();
        dto.setArtistName(entry.getArtistName());
        dto.setSongName(entry.getSongName());
        dto.setAlbumName(entry.getAlbumName());
        dto.setImageUrl(entry.getImageUrl());
        dto.setNbPlayed(entry.getNbPlayed());
        dto.setDatePlayed(entry.getDatePlayed());
        return dto;
    }

    public List<HIndexHistoryDto> getArtistHIndexHistory(Date dateStart, Date dateEnd) {
        List<HIndexHistoryDto> history = new ArrayList<>();

        if (dateStart == null || dateEnd == null || !dateStart.before(dateEnd)) {
            return history;
        }

        Calendar cursor = Calendar.getInstance();
        cursor.setTime(dateStart);

        Map<Integer, HIndexHistoryDto> reached = new LinkedHashMap<>();

        while (cursor.getTime().before(dateEnd)) {
            Date snapshotEndExclusive = addOneDay(cursor.getTime());

            HIndexSummaryProjection summary = playEntryDepot.getArtistHIndexSummary(dateStart, snapshotEndExclusive);
            int currentH = extractSafeHIndex(summary);

            if (currentH > 0 && !reached.containsKey(currentH)) {
                List<HIndexArtistProjection> details = playEntryDepot.getArtistHIndexDetails(dateStart, snapshotEndExclusive);

                HIndexHistoryDto dto = new HIndexHistoryDto();
                dto.setHIndex(currentH);
                dto.setReachedAt(cursor.getTime());
                dto.setCoreArtists(mapCoreArtistsAtMoment(details, currentH));

                reached.put(currentH, dto);
            }

            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        history.addAll(reached.values());
        history.sort(Comparator.comparing(HIndexHistoryDto::getHIndex));
        return history;
    }

    private int extractSafeHIndex(HIndexSummaryProjection summary) {
        if (summary == null || summary.getHIndex() == null) {
            return 0;
        }

        return summary.getHIndex();
    }

    private List<HIndexHistoryDto.HIndexHistoryArtistDto> mapCoreArtistsAtMoment(List<HIndexArtistProjection> details, int currentH) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyList();
        }

        return details.stream()
                .filter(Objects::nonNull)
                .filter(d -> Boolean.TRUE.equals(d.getInHIndex()))
                .filter(d -> d.getRankPosition() != null && d.getRankPosition() <= currentH)
                .sorted(Comparator.comparing(HIndexArtistProjection::getRankPosition))
                .map(d -> {
                    HIndexHistoryDto.HIndexHistoryArtistDto artistDto = new HIndexHistoryDto.HIndexHistoryArtistDto();
                    artistDto.setArtistName(d.getArtistName());
                    artistDto.setPlayCount(d.getPlayCount());
                    artistDto.setRankPosition(d.getRankPosition());
                    artistDto.setImageUrl(d.getImageUrl());
                    artistDto.setTopAlbumName(d.getTopAlbumName());
                    return artistDto;
                })
                .collect(Collectors.toList());
    }

    private Date addOneDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    private String normalizeAlbumSearchKey(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim()
                .replace("’", "'")
                .replace("`", "'")
                .replace("‐", "-")
                .replace("–", "-")
                .replace("—", "-")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);

        normalized = normalized.replaceAll("\\s*[-–—]\\s*ep$", "");
        normalized = normalized.replaceAll("\\s*[-–—]\\s*single$", "");
        normalized = normalized.replaceAll("\\(ep\\)$", "");
        normalized = normalized.replaceAll("\\(single\\)$", "");
        normalized = normalized.replaceAll("\\s*[-–—]\\s*deluxe edition$", "");
        normalized = normalized.replaceAll("\\s*[-–—]\\s*expanded edition$", "");
        normalized = normalized.replaceAll("\\s*[-–—]\\s*special edition$", "");
        normalized = normalized.replaceAll("\\s*[-–—]\\s*bonus version$", "");
        normalized = normalized.replaceAll("\\(deluxe edition\\)$", "");
        normalized = normalized.replaceAll("\\(expanded edition\\)$", "");
        normalized = normalized.replaceAll("\\(special edition\\)$", "");
        normalized = normalized.replaceAll("\\(bonus version\\)$", "");
        normalized = normalized.replaceAll("\\(remaster(?:ed)?(?: \\d{4})?\\)$", "");
        normalized = normalized.replaceAll("\\s*[-–—]\\s*remaster(?:ed)?(?: \\d{4})?$", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }



    public List<ArtistVariantSearchProjection> searchArtistVariants(String query, Integer limitValue) {
        String safeQuery = query == null ? "" : query.trim();

        if (safeQuery.isEmpty()) {
            return Collections.emptyList();
        }

        int safeLimit = (limitValue == null || limitValue <= 0) ? 20 : limitValue;

        return playEntryDepot.searchArtistVariantsKeepingCanonical(safeQuery, safeLimit);
    }
}