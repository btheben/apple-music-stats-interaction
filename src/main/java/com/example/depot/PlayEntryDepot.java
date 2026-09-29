package com.example.depot;

import com.example.modele.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface PlayEntryDepot extends JpaRepository<AppleMusicEntry, Long> {

    // =========================================================
    // LEGACY - à garder temporairement pour éviter de tout casser
    // =========================================================

    List<AppleMusicEntry> findByArtistNameIgnoreCaseOrderByDatePlayedDesc(String artistName);

    List<AppleMusicEntry> findByArtistNameIgnoreCaseAndSongNameIgnoreCaseOrderByDatePlayedDesc(String artistName, String songName);

    ArtistKeys findFirstByArtistName(String artistName);

    @Query("""
        SELECT new com.example.modele.Song(
            e.artistName,
            e.songName,
            e.albumName,
            e.dateAdded,
            MAX(e.datePlayed),
            e.length,
            MAX(e.nbPlayed)
        )
        FROM AppleMusicEntry e
        WHERE UPPER(e.artistName) = UPPER(:artistName)
        GROUP BY e.artistName, e.songName, e.albumName, e.dateAdded, e.length
        ORDER BY MAX(e.nbPlayed) DESC
        """)
    List<Song> findTopSongsByArtistName(@Param("artistName") String artistName);

    @Query(value = """
        SELECT
            MIN(trim(pe.song_name)) AS songName,
            MAX(pe.nb_played) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
          AND ex.artist_name IS NULL
        GROUP BY lower(trim(pe.song_name))
        ORDER BY MAX(pe.nb_played) DESC, MIN(trim(pe.song_name))
        """, nativeQuery = true)
    List<ArtistSongPlayCountProjection> getSongPlayCountsByArtist(@Param("artistName") String artistName);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                lower(trim(pe.song_name)) AS normalized_song_name,
                MAX(pe.nb_played) AS song_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.song_name))
        )
        SELECT
            MIN(trim(pe.artist_name)) AS artistName,
            COALESCE((SELECT SUM(sr.song_play_count) FROM song_rollup sr), 0) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
          AND ex.artist_name IS NULL
        """, nativeQuery = true)
    ArtistPresenceProjection getArtistPresence(@Param("artistName") String artistName);

    @Query("""
        select
            min(e.artistName),
            min(e.albumName),
            count(e),
            min(e.datePlayed),
            max(e.datePlayed),
            count(distinct e.songName),
            max(e.imageUrl)
        from AppleMusicEntry e
        where lower(trim(e.artistName)) = lower(trim(:artistName))
          and lower(trim(e.albumName)) = lower(trim(:albumName))
        """)
    List<Object[]> getAlbumSummaryRaw(@Param("artistName") String artistName,
                                      @Param("albumName") String albumName);

    @Query("""
        select new com.example.modele.AlbumSongStats(
            min(e.songName),
            count(e),
            min(e.datePlayed),
            max(e.datePlayed)
        )
        from AppleMusicEntry e
        where lower(trim(e.artistName)) = lower(trim(:artistName))
          and lower(trim(e.albumName)) = lower(trim(:albumName))
        group by lower(trim(e.songName))
        order by count(e) desc, min(e.songName)
        """)
    List<AlbumSongStats> getTopSongsForAlbum(@Param("artistName") String artistName,
                                             @Param("albumName") String albumName);

    @Query("""
        select new com.example.modele.AlbumPlayEntry(
            e.songName,
            e.datePlayed,
            e.nbPlayed
        )
        from AppleMusicEntry e
        where lower(trim(e.artistName)) = lower(trim(:artistName))
          and lower(trim(e.albumName)) = lower(trim(:albumName))
        order by e.datePlayed desc
        """)
    List<AlbumPlayEntry> getRecentPlaysForAlbum(@Param("artistName") String artistName,
                                                @Param("albumName") String albumName,
                                                Pageable pageable);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                lower(trim(pe.album_name)) AS normalized_album_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.album_name) AS album_name,
                MAX(pe.nb_played) AS song_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.album_name)), lower(trim(pe.song_name))
        ),
        album_counts AS (
            SELECT
                album_name AS albumName,
                SUM(song_play_count) AS playCount
            FROM song_rollup
            GROUP BY album_name
        ),
        ranked AS (
            SELECT
                albumName,
                playCount,
                ROW_NUMBER() OVER (ORDER BY playCount DESC, albumName) AS rankPosition
            FROM album_counts
        )
        SELECT
            rankPosition,
            albumName,
            playCount
        FROM ranked
        WHERE lower(trim(albumName)) = lower(trim(:albumName))
        """, nativeQuery = true)
    AlbumRankProjection getAlbumRankForArtist(@Param("artistName") String artistName,
                                              @Param("albumName") String albumName);

    @Query(value = """
        SELECT
            MIN(pe.song_name) AS songName,
            MAX(pe.nb_played) AS playCount,
            MIN(pe.date_played) AS firstPlayedAt,
            MAX(pe.date_played) AS lastPlayedAt
        FROM play_entry pe
        WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
          AND lower(trim(pe.album_name)) = lower(trim(:albumName))
        GROUP BY lower(trim(pe.song_name))
        ORDER BY MAX(pe.nb_played) DESC, MIN(pe.song_name)
        """, nativeQuery = true)
    List<AlbumSongStatsProjection> getAlbumSongStats(@Param("artistName") String artistName,
                                                     @Param("albumName") String albumName);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.song_name) AS song_name,
                MAX(pe.nb_played) AS historical_play_count
            FROM play_entry pe
            WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
              AND lower(trim(pe.album_name)) = lower(trim(:albumName))
            GROUP BY lower(trim(pe.song_name))
        )
        SELECT
            MIN(pe.artist_name) AS artistName,
            MIN(pe.album_name) AS albumName,
            COUNT(*) AS trackedPlayCount,
            COALESCE((SELECT SUM(sr.historical_play_count) FROM song_rollup sr), 0) AS historicalPlayCount,
            MIN(pe.date_played) AS firstPlayedAt,
            MAX(pe.date_played) AS lastPlayedAt,
            COUNT(DISTINCT lower(trim(pe.song_name))) AS distinctSongCount,
            MAX(pe.image_url) AS imageUrl
        FROM play_entry pe
        WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
          AND lower(trim(pe.album_name)) = lower(trim(:albumName))
        """, nativeQuery = true)
    AlbumSummaryProjection getAlbumSummary(@Param("artistName") String artistName,
                                           @Param("albumName") String albumName);

    // =========================================================
    // NORMALIZED - nouvelles méthodes à privilégier maintenant
    // =========================================================

    @Query("""
        select e
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
        order by e.datePlayed desc
        """)
    List<AppleMusicEntry> findByArtistNameNormalizedOrderByDatePlayedDesc(@Param("artistNameNormalized") String artistNameNormalized);

    @Query("""
        select e
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
          and lower(trim(e.songName)) = lower(trim(:songName))
        order by e.datePlayed desc
        """)
    List<AppleMusicEntry> findByArtistNameNormalizedAndSongNameOrderByDatePlayedDesc(@Param("artistNameNormalized") String artistNameNormalized,
                                                                                     @Param("songName") String songName);

    @Query("""
        select new com.example.modele.Song(
            min(e.artistName),
            min(e.songName),
            min(e.albumName),
            min(e.dateAdded),
            max(e.datePlayed),
            min(e.length),
            max(e.nbPlayed)
        )
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
        group by lower(trim(e.songName))
        order by max(e.nbPlayed) desc
        """)
    List<Song> findTopSongsByArtistNameNormalized(@Param("artistNameNormalized") String artistNameNormalized);

    @Query("""
        select min(e.artistName)
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
        """)
    String findDisplayArtistNameByNormalized(@Param("artistNameNormalized") String artistNameNormalized);

    @Query(value = """
        SELECT
            MIN(trim(pe.song_name)) AS songName,
            MAX(pe.nb_played) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.artist_name_normalized = :artistNameNormalized
          AND ex.artist_name IS NULL
        GROUP BY lower(trim(pe.song_name))
        ORDER BY MAX(pe.nb_played) DESC, MIN(trim(pe.song_name))
        """, nativeQuery = true)
    List<ArtistSongPlayCountProjection> getSongPlayCountsByArtistNormalized(@Param("artistNameNormalized") String artistNameNormalized);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                lower(trim(pe.song_name)) AS normalized_song_name,
                MAX(pe.nb_played) AS song_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE pe.artist_name_normalized = :artistNameNormalized
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.song_name))
        )
        SELECT
            MIN(pe.artist_name) AS artistName,
            COALESCE((SELECT SUM(sr.song_play_count) FROM song_rollup sr), 0) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.artist_name_normalized = :artistNameNormalized
          AND ex.artist_name IS NULL
        """, nativeQuery = true)
    ArtistPresenceProjection getArtistPresenceNormalized(@Param("artistNameNormalized") String artistNameNormalized);

    @Query("""
        select
            min(e.artistName),
            min(e.albumName),
            count(e),
            min(e.datePlayed),
            max(e.datePlayed),
            count(distinct e.songName),
            max(e.imageUrl)
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
          and lower(trim(e.albumName)) = lower(trim(:albumName))
        """)
    List<Object[]> getAlbumSummaryRawNormalized(@Param("artistNameNormalized") String artistNameNormalized,
                                                @Param("albumName") String albumName);

    @Query("""
        select new com.example.modele.AlbumSongStats(
            min(e.songName),
            count(e),
            min(e.datePlayed),
            max(e.datePlayed)
        )
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
          and lower(trim(e.albumName)) = lower(trim(:albumName))
        group by lower(trim(e.songName))
        order by count(e) desc, min(e.songName)
        """)
    List<AlbumSongStats> getTopSongsForAlbumNormalized(@Param("artistNameNormalized") String artistNameNormalized,
                                                       @Param("albumName") String albumName);

    @Query("""
        select new com.example.modele.AlbumPlayEntry(
            e.songName,
            e.datePlayed,
            e.nbPlayed
        )
        from AppleMusicEntry e
        where e.artistNameNormalized = :artistNameNormalized
          and lower(trim(e.albumName)) = lower(trim(:albumName))
        order by e.datePlayed desc
        """)
    List<AlbumPlayEntry> getRecentPlaysForAlbumNormalized(@Param("artistNameNormalized") String artistNameNormalized,
                                                          @Param("albumName") String albumName,
                                                          Pageable pageable);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                lower(trim(pe.album_name)) AS normalized_album_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.album_name) AS album_name,
                MAX(pe.nb_played) AS song_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE pe.artist_name_normalized = :artistNameNormalized
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.album_name)), lower(trim(pe.song_name))
        ),
        album_counts AS (
            SELECT
                album_name AS albumName,
                SUM(song_play_count) AS playCount
            FROM song_rollup
            GROUP BY album_name
        ),
        ranked AS (
            SELECT
                albumName,
                playCount,
                ROW_NUMBER() OVER (ORDER BY playCount DESC, albumName) AS rankPosition
            FROM album_counts
        )
        SELECT
            rankPosition,
            albumName,
            playCount
        FROM ranked
        WHERE lower(trim(albumName)) = lower(trim(:albumName))
        """, nativeQuery = true)
    AlbumRankProjection getAlbumRankForArtistNormalized(@Param("artistNameNormalized") String artistNameNormalized,
                                                        @Param("albumName") String albumName);

    @Query(value = """
        SELECT
            MIN(pe.song_name) AS songName,
            MAX(pe.nb_played) AS playCount,
            MIN(pe.date_played) AS firstPlayedAt,
            MAX(pe.date_played) AS lastPlayedAt
        FROM play_entry pe
        WHERE pe.artist_name_normalized = :artistNameNormalized
          AND lower(trim(pe.album_name)) = lower(trim(:albumName))
        GROUP BY lower(trim(pe.song_name))
        ORDER BY MAX(pe.nb_played) DESC, MIN(pe.song_name)
        """, nativeQuery = true)
    List<AlbumSongStatsProjection> getAlbumSongStatsNormalized(@Param("artistNameNormalized") String artistNameNormalized,
                                                               @Param("albumName") String albumName);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.song_name) AS song_name,
                MAX(pe.nb_played) AS historical_play_count
            FROM play_entry pe
            WHERE pe.artist_name_normalized = :artistNameNormalized
              AND lower(trim(pe.album_name)) = lower(trim(:albumName))
            GROUP BY lower(trim(pe.song_name))
        )
        SELECT
            MIN(pe.artist_name) AS artistName,
            MIN(pe.album_name) AS albumName,
            COUNT(*) AS trackedPlayCount,
            COALESCE((SELECT SUM(sr.historical_play_count) FROM song_rollup sr), 0) AS historicalPlayCount,
            MIN(pe.date_played) AS firstPlayedAt,
            MAX(pe.date_played) AS lastPlayedAt,
            COUNT(DISTINCT lower(trim(pe.song_name))) AS distinctSongCount,
            MAX(pe.image_url) AS imageUrl
        FROM play_entry pe
        WHERE pe.artist_name_normalized = :artistNameNormalized
          AND lower(trim(pe.album_name)) = lower(trim(:albumName))
        """, nativeQuery = true)
    AlbumSummaryProjection getAlbumSummaryNormalized(@Param("artistNameNormalized") String artistNameNormalized,
                                                     @Param("albumName") String albumName);

    // =========================================================
    // GLOBAL STATS / DASHBOARD - déjà majoritairement en normalized
    // =========================================================

    @Query(value = """
    with day as (
        select *
        from play_entry pe
        where date_played >= :dateStart
          and date_played < :dateEnd
    ),
    images as (
        select distinct on (artist_name, song_name)
            artist_name,
            song_name,
            image_url
        from play_entry pe
        where image_url is not null
    )
    select
        d.id,
        d.artist_name,
        d.artist_name_normalized,
        d.song_name,
        d.album_name,
        d.date_added,
        d.date_played,
        d.length,
        d.nb_played,
        d.genre,
        d.rating,
        i.image_url
    from day d
    left join images i
        on d.artist_name = i.artist_name
       and d.song_name = i.song_name
    order by d.date_played desc
    """, nativeQuery = true)
    List<AppleMusicEntry> findByDatePlayedBetweenOrderByDatePlayedDesc(
            @Param("dateStart") Date dateStart,
            @Param("dateEnd") Date dateEnd
    );

    @Query("""
    select new com.example.modele.TopArtist(
        min(e.artistName),
        count(e),
        max(e.imageUrl)
    )
    from AppleMusicEntry e
    where e.datePlayed >= :dateStart
      and e.datePlayed < :dateEnd
      and e.artistNameNormalized is not null
      and not exists (
          select 1
          from Exclusion a
          where a.artistNameNormalized = e.artistNameNormalized
      )
    group by e.artistNameNormalized
    having count(e) > 1
    order by count(e) desc, min(e.artistName)
    """)
    List<TopArtist> getTopArtistDateInterval(@Param("dateStart") Date dateStart,
                                             @Param("dateEnd") Date dateEnd);

    @Query(value = "with test as (Select artist_name, nb_played from play_entry where id = 34311) select new com.example.modele.TopArtist(artist_name, nb_played) from test ", nativeQuery = true)
    List<TopArtist> getTopArtistAllTime();

    @Query("""
        select new com.example.modele.TopSong(
            e.artistName,
            e.songName,
            count(e),
            max(e.imageUrl)
        )
        from AppleMusicEntry e
        where e.datePlayed >= :dateStart
          and e.datePlayed < :dateEnd
          and e.artistNameNormalized is not null
          and not exists (
              select 1
              from Exclusion a
              where a.artistNameNormalized = e.artistNameNormalized
          )
        group by e.artistName, e.songName
        having count(e) > 1
        order by count(e) desc, e.artistName
        """)
    List<TopSong> getTopSongDateInterval(@Param("dateStart") Date dateStart,
                                         @Param("dateEnd") Date dateEnd);

    @Query("""
        select new com.example.modele.TopSong(
            min(e.artistName),
            min(e.songName),
            count(e),
            max(e.imageUrl)
        )
        from AppleMusicEntry e
        where e.datePlayed >= :dateStart
          and e.datePlayed < :dateEnd
          and e.artistNameNormalized is not null
          and not exists (
              select 1
              from Exclusion a
              where a.artistNameNormalized = e.artistNameNormalized
          )
        group by e.artistNameNormalized, lower(trim(e.songName))
        having count(e) > 1
        order by count(e) desc, min(e.artistName)
        """)
    List<TopSong> getTopSongDateInterval(@Param("dateStart") Date dateStart,
                                         @Param("dateEnd") Date dateEnd,
                                         Pageable pageable);

    @Query("""
        select new com.example.modele.TopAlbum(
            min(e.artistName),
            min(e.albumName),
            count(e),
            max(e.imageUrl)
        )
        from AppleMusicEntry e
        where e.datePlayed >= :dateStart
          and e.datePlayed < :dateEnd
          and e.artistNameNormalized is not null
          and not exists (
              select 1
              from Exclusion a
              where a.artistNameNormalized = e.artistNameNormalized
          )
        group by e.artistNameNormalized, lower(trim(e.albumName))
        having count(e) > 1
        order by count(e) desc, min(e.artistName)
        """)
    List<TopAlbum> getTopAlbumDateInterval(@Param("dateStart") Date dateStart,
                                           @Param("dateEnd") Date dateEnd,
                                           Pageable pageable);

    @Query("""
    select new com.example.modele.TopArtist(
        min(e.artistName),
        count(e),
        max(e.imageUrl)
    )
    from AppleMusicEntry e
    where e.datePlayed >= :dateStart
      and e.datePlayed < :dateEnd
      and e.artistNameNormalized is not null
      and not exists (
          select 1
          from Exclusion a
          where a.artistNameNormalized = e.artistNameNormalized
      )
    group by e.artistNameNormalized
    having count(e) > 1
    order by count(e) desc, min(e.artistName)
    """)
    List<TopArtist> getTopArtistDateInterval(@Param("dateStart") Date dateStart,
                                             @Param("dateEnd") Date dateEnd,
                                             Pageable pageable);

    @Query("""
        select new com.example.modele.TopAlbum(
            min(e.artistName),
            min(e.albumName),
            count(e),
            max(e.imageUrl)
        )
        from AppleMusicEntry e
        where e.datePlayed >= :dateStart
          and e.datePlayed < :dateEnd
          and e.artistNameNormalized is not null
          and not exists (
              select 1
              from Exclusion a
              where a.artistNameNormalized = e.artistNameNormalized
          )
        group by e.artistNameNormalized, lower(trim(e.albumName))
        having count(e) > 1
        order by count(e) desc, min(e.artistName)
        """)
    List<TopAlbum> getTopAlbumDateInterval(@Param("dateStart") Date dateStart,
                                           @Param("dateEnd") Date dateEnd);

    @Query("""
        select new com.example.modele.TopArtist(
            min(e.artistName),
            count(e)
        )
        from AppleMusicEntry e
        where e.datePlayed >= :dateEnd
          and e.artistNameNormalized is not null
          and not exists (
              select 1
              from Exclusion a
              where a.artistNameNormalized = e.artistNameNormalized
          )
        group by e.artistNameNormalized
        having count(e) >= 2
        order by count(e) desc, min(e.artistName)
        """)
    List<TopArtist> getTopArtistsLastWeek(@Param("dateEnd") Date dateEnd);

    @Query("""
        select new com.example.modele.TopSong(
            e.artistName,
            e.songName,
            count(e)
        )
        from AppleMusicEntry e
        where e.datePlayed >= :dateEnd
          and e.artistNameNormalized is not null
          and not exists (
              select 1
              from Exclusion a
              where a.artistNameNormalized = e.artistNameNormalized
          )
        group by e.artistName, e.songName
        having count(e) >= 2
        order by count(e) desc, e.artistName
        """)
    List<TopSong> getTopSongLastWeek(@Param("dateEnd") Date dateEnd);

    @Query(value = """
        SELECT
            d.day AS day,
            COUNT(pe.id) AS playCount
        FROM generate_series(
            CAST(:dateStart AS date),
            CAST(:dateEnd AS date),
            interval '1 day'
        ) AS d(day)
        LEFT JOIN play_entry pe
            ON pe.date_played >= d.day
           AND pe.date_played < d.day + interval '1 day'
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE ex.artist_name IS NULL OR pe.id IS NULL
        GROUP BY d.day
        ORDER BY d.day
        """, nativeQuery = true)
    List<DailyPlayCountProjection> getDailyPlayCountBetween(@Param("dateStart") Date dateStart,
                                                            @Param("dateEnd") Date dateEnd);

    @Query(value = """
        SELECT
            DATE(pe.date_played) AS period,
            COUNT(*) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.date_played >= :dateStart
          AND pe.date_played < :dateEnd
          AND ex.artist_name IS NULL
        GROUP BY DATE(pe.date_played)
        ORDER BY playCount DESC, period DESC
        LIMIT 10
        """, nativeQuery = true)
    List<PeriodPlayCountProjection> getTop10Days(@Param("dateStart") Date dateStart,
                                                 @Param("dateEnd") Date dateEnd);

    @Query(value = """
        SELECT
            DATE_TRUNC('month', pe.date_played)::date AS period,
            COUNT(*) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.date_played >= :dateStart
          AND pe.date_played < :dateEnd
          AND ex.artist_name IS NULL
        GROUP BY DATE_TRUNC('month', pe.date_played)
        ORDER BY playCount DESC, period DESC
        LIMIT 10
        """, nativeQuery = true)
    List<PeriodPlayCountProjection> getTop10Months(@Param("dateStart") Date dateStart,
                                                   @Param("dateEnd") Date dateEnd);

    @Query(value = """
        SELECT
            DATE_TRUNC('year', pe.date_played)::date AS period,
            COUNT(*) AS playCount
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.date_played >= :dateStart
          AND pe.date_played < :dateEnd
          AND ex.artist_name IS NULL
        GROUP BY DATE_TRUNC('year', pe.date_played)
        ORDER BY playCount DESC, period DESC
        LIMIT 10
        """, nativeQuery = true)
    List<PeriodPlayCountProjection> getTop10Years(@Param("dateStart") Date dateStart,
                                                  @Param("dateEnd") Date dateEnd);

    @Query(value = """
    WITH new_artists AS (
        SELECT
            pe.artist_name_normalized,
            MIN(pe.artist_name) AS artist_name,
            COUNT(*) AS play_count,
            MAX(pe.image_url) AS image_url,
            MIN(pe.date_played) AS first_played_date
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.date_played >= :dateStart
          AND pe.date_played < :dateEnd
          AND pe.artist_name_normalized IS NOT NULL
          AND ex.artist_name IS NULL
          AND NOT EXISTS (
              SELECT 1
              FROM play_entry old_pe
              WHERE old_pe.artist_name_normalized = pe.artist_name_normalized
                AND old_pe.date_played < :dateStart
          )
        GROUP BY pe.artist_name_normalized
    ),
    artist_song_counts AS (
        SELECT
            pe.artist_name_normalized,
            lower(trim(pe.song_name)) AS normalized_song_name,
            MIN(pe.song_name) AS song_name,
            COUNT(*) AS song_play_count
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE pe.date_played >= :dateStart
          AND pe.date_played < :dateEnd
          AND pe.artist_name_normalized IS NOT NULL
          AND ex.artist_name IS NULL
          AND pe.song_name IS NOT NULL
          AND trim(pe.song_name) <> ''
          AND pe.artist_name_normalized IN (
              SELECT artist_name_normalized
              FROM new_artists
          )
        GROUP BY pe.artist_name_normalized, lower(trim(pe.song_name))
    ),
    top_song_per_artist AS (
        SELECT
            artist_name_normalized,
            song_name
        FROM (
            SELECT
                asc1.artist_name_normalized,
                asc1.song_name,
                asc1.song_play_count,
                ROW_NUMBER() OVER (
                    PARTITION BY asc1.artist_name_normalized
                    ORDER BY asc1.song_play_count DESC, asc1.song_name
                ) AS rn
            FROM artist_song_counts asc1
        ) ranked
        WHERE rn = 1
    )
    SELECT
        na.artist_name AS artistName,
        na.play_count AS playCount,
        na.image_url AS imageUrl,
        na.first_played_date AS firstPlayedDate,
        ts.song_name AS topSongName
    FROM new_artists na
    LEFT JOIN top_song_per_artist ts
        ON ts.artist_name_normalized = na.artist_name_normalized
    ORDER BY na.play_count DESC, na.artist_name
    LIMIT :limitValue
    """, nativeQuery = true)
    List<NewArtistProjection> getTopNewArtists(@Param("dateStart") Date dateStart,
                                               @Param("dateEnd") Date dateEnd,
                                               @Param("limitValue") Integer limitValue);

    @Query(value = """
        WITH song_rollup AS (
            SELECT
                pe.artist_name_normalized AS normalized_artist_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.artist_name) AS artist_name,
                MAX(pe.nb_played) AS song_play_count,
                MAX(pe.date_played) AS last_played
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE ex.artist_name IS NULL
              AND pe.artist_name_normalized IS NOT NULL
            GROUP BY pe.artist_name_normalized, lower(trim(pe.song_name))
        ),
        artist_rollup AS (
            SELECT
                artist_name AS artistName,
                SUM(song_play_count)::int AS totalPlays,
                MAX(last_played) AS lastPlayed,
                (CURRENT_DATE - MAX(last_played)::date) AS daysSinceLastPlay
            FROM song_rollup
            GROUP BY artist_name
        )
        SELECT
            ar.artistName AS artistName,
            ar.totalPlays AS totalPlays,
            ar.lastPlayed AS lastPlayed,
            ar.daysSinceLastPlay AS daysSinceLastPlay
        FROM artist_rollup ar
        WHERE ar.lastPlayed < CURRENT_DATE - interval '1 year'
          AND ar.totalPlays >= :minPlays
        ORDER BY RANDOM()
        LIMIT :limitValue
        """, nativeQuery = true)
    List<RediscoveryArtistProjection> getRandomRediscoveryArtists(@Param("minPlays") Integer minPlays,
                                                                  @Param("limitValue") Integer limitValue);

    @Query(value = """
        SELECT
            d.period AS period,
            COUNT(pe.id) AS playCount
        FROM generate_series(
            date_trunc('month', CAST(:dateStart AS date)),
            date_trunc('month', CAST(:dateEnd AS date)),
            interval '1 month'
        ) AS d(period)
        LEFT JOIN play_entry pe
            ON pe.date_played >= d.period
           AND pe.date_played < d.period + interval '1 month'
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE ex.artist_name IS NULL OR pe.id IS NULL
        GROUP BY d.period
        ORDER BY d.period
        """, nativeQuery = true)
    List<PeriodPlayCountProjection> getMonthlyPlayCountBetween(@Param("dateStart") Date dateStart,
                                                               @Param("dateEnd") Date dateEnd);

    @Query(value = """
        SELECT
            d.period AS period,
            COUNT(pe.id) AS playCount
        FROM generate_series(
            date_trunc('year', CAST(:dateStart AS date)),
            date_trunc('year', CAST(:dateEnd AS date)),
            interval '1 year'
        ) AS d(period)
        LEFT JOIN play_entry pe
            ON pe.date_played >= d.period
           AND pe.date_played < d.period + interval '1 year'
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE ex.artist_name IS NULL OR pe.id IS NULL
        GROUP BY d.period
        ORDER BY d.period
        """, nativeQuery = true)
    List<PeriodPlayCountProjection> getYearlyPlayCountBetween(@Param("dateStart") Date dateStart,
                                                              @Param("dateEnd") Date dateEnd);

    @Query(value = """
        WITH filtered_entries AS (
            SELECT
                pe.artist_name_normalized,
                pe.artist_name,
                pe.album_name,
                pe.image_url,
                pe.date_played
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE pe.date_played >= :dateStart
              AND pe.date_played < :dateEnd
              AND pe.artist_name_normalized IS NOT NULL
              AND ex.artist_name IS NULL
        ),
        artist_counts AS (
            SELECT
                artist_name_normalized,
                MIN(artist_name) AS artist_name,
                COUNT(*) AS play_count
            FROM filtered_entries
            GROUP BY artist_name_normalized
        ),
        ranked AS (
            SELECT
                artist_name_normalized,
                artist_name,
                play_count,
                ROW_NUMBER() OVER (ORDER BY play_count DESC, artist_name) AS rank_position
            FROM artist_counts
        ),
        h_value AS (
            SELECT COALESCE(MAX(rank_position), 0) AS h_index
            FROM ranked
            WHERE play_count >= rank_position
        ),
        artist_images AS (
            SELECT
                artist_name_normalized,
                MAX(image_url) AS image_url
            FROM filtered_entries
            WHERE image_url IS NOT NULL
              AND trim(image_url) <> ''
            GROUP BY artist_name_normalized
        ),
        album_counts AS (
            SELECT
                artist_name_normalized,
                album_name,
                COUNT(*) AS album_play_count
            FROM filtered_entries
            WHERE album_name IS NOT NULL
              AND trim(album_name) <> ''
            GROUP BY artist_name_normalized, album_name
        ),
        top_album AS (
            SELECT
                artist_name_normalized,
                album_name
            FROM (
                SELECT
                    artist_name_normalized,
                    album_name,
                    album_play_count,
                    ROW_NUMBER() OVER (
                        PARTITION BY artist_name_normalized
                        ORDER BY album_play_count DESC, album_name
                    ) AS rn
                FROM album_counts
            ) ranked_albums
            WHERE rn = 1
        )
        SELECT
            r.artist_name AS artistName,
            r.play_count AS playCount,
            r.rank_position AS rankPosition,
            h.h_index AS hIndex,
            GREATEST((h.h_index + 1) - r.play_count, 0) AS playsNeededForNextH,
            CASE
                WHEN r.rank_position <= h.h_index
                     AND r.play_count >= h.h_index THEN true
                ELSE false
            END AS inHIndex,
            ai.image_url AS imageUrl,
            ta.album_name AS topAlbumName
        FROM ranked r
        CROSS JOIN h_value h
        LEFT JOIN artist_images ai
            ON ai.artist_name_normalized = r.artist_name_normalized
        LEFT JOIN top_album ta
            ON ta.artist_name_normalized = r.artist_name_normalized
        WHERE r.rank_position <= GREATEST(h.h_index + 10, 10)
        ORDER BY r.rank_position
        """, nativeQuery = true)
    List<HIndexArtistProjection> getArtistHIndexDetails(@Param("dateStart") Date dateStart,
                                                        @Param("dateEnd") Date dateEnd);

    @Query(value = """
        WITH artist_counts AS (
            SELECT
                pe.artist_name_normalized,
                MIN(pe.artist_name) AS artist_name,
                COUNT(*) AS play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE pe.date_played >= :dateStart
              AND pe.date_played < :dateEnd
              AND pe.artist_name_normalized IS NOT NULL
              AND ex.artist_name IS NULL
            GROUP BY pe.artist_name_normalized
        ),
        ranked AS (
            SELECT
                artist_name_normalized,
                artist_name,
                play_count,
                ROW_NUMBER() OVER (ORDER BY play_count DESC, artist_name) AS rank_position
            FROM artist_counts
        ),
        h_value AS (
            SELECT COALESCE(MAX(rank_position), 0) AS h_index
            FROM ranked
            WHERE play_count >= rank_position
        )
        SELECT
            h.h_index AS hIndex,
            COUNT(*) FILTER (WHERE r.play_count < h.h_index + 1) AS artistsNeededForNextH
        FROM h_value h
        LEFT JOIN ranked r
            ON r.rank_position <= h.h_index + 1
        GROUP BY h.h_index
        """, nativeQuery = true)
    HIndexSummaryProjection getArtistHIndexSummary(@Param("dateStart") Date dateStart,
                                                   @Param("dateEnd") Date dateEnd);

    @Query(value = """
        WITH recent_plays AS (
            SELECT
                pe.artist_name_normalized AS normalized_artist_name,
                MIN(pe.artist_name) AS artist_name,
                MIN(pe.date_played) AS first_recent_play,
                MAX(pe.date_played) AS last_recent_play,
                COUNT(*) AS recent_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE pe.date_played >= :dateStart
              AND pe.date_played < :dateEnd
              AND pe.artist_name_normalized IS NOT NULL
              AND ex.artist_name IS NULL
            GROUP BY pe.artist_name_normalized
        ),
        total_song_rollup AS (
            SELECT
                pe.artist_name_normalized AS normalized_artist_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MAX(pe.nb_played) AS song_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE ex.artist_name IS NULL
              AND pe.artist_name_normalized IS NOT NULL
            GROUP BY pe.artist_name_normalized, lower(trim(pe.song_name))
        ),
        total_artist_rollup AS (
            SELECT
                normalized_artist_name,
                SUM(song_play_count)::int AS total_play_count
            FROM total_song_rollup
            GROUP BY normalized_artist_name
        ),
        previous_plays AS (
            SELECT
                rp.artist_name,
                rp.normalized_artist_name,
                rp.first_recent_play,
                rp.last_recent_play,
                rp.recent_play_count,
                (
                    SELECT MAX(old_pe.date_played)
                    FROM play_entry old_pe
                    WHERE old_pe.artist_name_normalized = rp.normalized_artist_name
                      AND old_pe.date_played < rp.first_recent_play
                ) AS previous_play,
                COALESCE(tar.total_play_count, 0) AS total_play_count,
                (
                    SELECT pe2.song_name
                    FROM play_entry pe2
                    WHERE pe2.artist_name_normalized = rp.normalized_artist_name
                      AND pe2.date_played = rp.last_recent_play
                    LIMIT 1
                ) AS last_song_name
            FROM recent_plays rp
            LEFT JOIN total_artist_rollup tar
                ON tar.normalized_artist_name = rp.normalized_artist_name
        )
        SELECT
            pp.artist_name AS artistName,
            pp.first_recent_play AS firstRecentPlay,
            pp.last_recent_play AS lastRecentPlay,
            pp.previous_play AS previousPlay,
            EXTRACT(DAY FROM (pp.first_recent_play - pp.previous_play))::int AS daysWithoutListening,
            pp.recent_play_count AS recentPlayCount,
            pp.total_play_count AS totalPlayCount,
            pp.last_song_name AS lastSongName
        FROM previous_plays pp
        WHERE pp.previous_play IS NOT NULL
          AND pp.previous_play < pp.first_recent_play - (CAST(:minGapDays AS integer) * interval '1 day')
          AND pp.total_play_count >= :minTotalPlays
        ORDER BY pp.first_recent_play DESC
        LIMIT :limitValue
        """, nativeQuery = true)
    List<RecentRediscoveryProjection> getRecentRediscoveries(@Param("dateStart") Date dateStart,
                                                             @Param("dateEnd") Date dateEnd,
                                                             @Param("minGapDays") Integer minGapDays,
                                                             @Param("minTotalPlays") Integer minTotalPlays,
                                                             @Param("limitValue") Integer limitValue);

    @Query(value = """
        SELECT
            COUNT(*) AS artistCount
        FROM (
            SELECT pe.artist_name_normalized
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON ex.artist_name_normalized = pe.artist_name_normalized
            WHERE pe.date_played >= :dateStart
              AND pe.date_played < :dateEnd
              AND pe.artist_name_normalized IS NOT NULL
              AND ex.artist_name IS NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM play_entry old_pe
                  WHERE old_pe.artist_name_normalized = pe.artist_name_normalized
                    AND old_pe.date_played < :dateStart
              )
            GROUP BY pe.artist_name_normalized
        ) t
        """, nativeQuery = true)
    NewArtistCountProjection getNewArtistCount(@Param("dateStart") Date dateStart,
                                               @Param("dateEnd") Date dateEnd);

    @Modifying
    @Transactional
    @Query("""
        update AppleMusicEntry e
           set e.songName = :newSongName,
               e.albumName = :newAlbumName
         where lower(trim(e.artistName)) = lower(trim(:artistName))
           and lower(trim(e.songName)) = lower(trim(:oldSongName))
        """)
    int updateSongMetadata(@Param("artistName") String artistName,
                           @Param("oldSongName") String oldSongName,
                           @Param("newSongName") String newSongName,
                           @Param("newAlbumName") String newAlbumName);

    // =========================================================
    // MILESTONES - encore legacy pour l’instant, on les garde
    // =========================================================

    @Query(value = """
        WITH milestones AS (
            SELECT 5 AS milestone
            UNION ALL SELECT 10
            UNION ALL SELECT 20
            UNION ALL SELECT 30
            UNION ALL SELECT 50
            UNION ALL SELECT 75
            UNION ALL SELECT 100
            UNION ALL SELECT 150
            UNION ALL SELECT 200
            UNION ALL SELECT 250
            UNION ALL SELECT 300
        ),
        song_before AS (
            SELECT
                lower(trim(pe.artist_name)) AS normalized_artist_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MAX(pe.nb_played) AS previous_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON lower(trim(ex.artist_name)) = lower(trim(pe.artist_name))
            WHERE pe.date_played < :dateStart
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.artist_name)), lower(trim(pe.song_name))
        ),
        song_current AS (
            SELECT
                lower(trim(pe.artist_name)) AS normalized_artist_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.artist_name) AS artist_name,
                MIN(pe.song_name) AS song_name,
                MIN(pe.album_name) AS album_name,
                MAX(pe.image_url) AS image_url,
                MAX(pe.nb_played) AS current_play_count,
                COUNT(*) FILTER (
                    WHERE pe.date_played >= :dateStart
                      AND pe.date_played < :dateEnd
                ) AS recent_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON lower(trim(ex.artist_name)) = lower(trim(pe.artist_name))
            WHERE pe.date_played < :dateEnd
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.artist_name)), lower(trim(pe.song_name))
        ),
        crossed AS (
            SELECT
                sc.normalized_artist_name,
                sc.normalized_song_name,
                sc.artist_name,
                sc.song_name,
                sc.album_name,
                sc.image_url,
                sc.recent_play_count,
                COALESCE(sb.previous_play_count, 0) AS previous_play_count,
                sc.current_play_count,
                m.milestone
            FROM song_current sc
            LEFT JOIN song_before sb
                ON sb.normalized_artist_name = sc.normalized_artist_name
               AND sb.normalized_song_name = sc.normalized_song_name
            INNER JOIN milestones m
                ON COALESCE(sb.previous_play_count, 0) < m.milestone
               AND sc.current_play_count >= m.milestone
        ),
        reached_at AS (
            SELECT
                c.normalized_artist_name,
                c.normalized_song_name,
                c.milestone,
                MIN(pe.date_played) AS reached_at
            FROM crossed c
            JOIN play_entry pe
                ON lower(trim(pe.artist_name)) = c.normalized_artist_name
               AND lower(trim(pe.song_name)) = c.normalized_song_name
            LEFT JOIN exclusion_artist ex
                ON lower(trim(ex.artist_name)) = lower(trim(pe.artist_name))
            WHERE pe.nb_played >= c.milestone
              AND ex.artist_name IS NULL
            GROUP BY c.normalized_artist_name, c.normalized_song_name, c.milestone
        )
        SELECT
            c.artist_name AS artistName,
            c.song_name AS songName,
            c.album_name AS albumName,
            c.image_url AS imageUrl,
            c.previous_play_count AS previousPlayCount,
            c.current_play_count AS currentPlayCount,
            c.milestone AS milestoneReached,
            ra.reached_at AS reachedAt,
            c.recent_play_count AS recentPlayCount
        FROM crossed c
        LEFT JOIN reached_at ra
            ON ra.normalized_artist_name = c.normalized_artist_name
           AND ra.normalized_song_name = c.normalized_song_name
           AND ra.milestone = c.milestone
        ORDER BY c.milestone DESC, c.current_play_count DESC, c.artist_name, c.song_name
        """, nativeQuery = true)
    List<RecentMilestoneProjection> getRecentMilestones(@Param("dateStart") Date dateStart,
                                                        @Param("dateEnd") Date dateEnd);

    @Query(value = """
        WITH milestones AS (
            SELECT 50 AS milestone
            UNION ALL SELECT 75
            UNION ALL SELECT 100
            UNION ALL SELECT 150
            UNION ALL SELECT 200
            UNION ALL SELECT 250
            UNION ALL SELECT 300
        ),
        song_current AS (
            SELECT
                lower(trim(pe.artist_name)) AS normalized_artist_name,
                lower(trim(pe.song_name)) AS normalized_song_name,
                MIN(pe.artist_name) AS artist_name,
                MIN(pe.song_name) AS song_name,
                MIN(pe.album_name) AS album_name,
                MAX(pe.image_url) AS image_url,
                MAX(pe.nb_played) AS current_play_count,
                COUNT(*) FILTER (
                    WHERE pe.date_played >= :recentDateStart
                      AND pe.date_played < :dateEnd
                ) AS recent_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON lower(trim(ex.artist_name)) = lower(trim(pe.artist_name))
            WHERE pe.date_played < :dateEnd
              AND ex.artist_name IS NULL
            GROUP BY lower(trim(pe.artist_name)), lower(trim(pe.song_name))
        ),
        candidates AS (
            SELECT
                sc.artist_name,
                sc.song_name,
                sc.album_name,
                sc.image_url,
                sc.current_play_count,
                sc.recent_play_count,
                m.milestone,
                (m.milestone - sc.current_play_count) AS plays_needed,
                ROW_NUMBER() OVER (
                    PARTITION BY sc.normalized_artist_name, sc.normalized_song_name
                    ORDER BY m.milestone
                ) AS rn
            FROM song_current sc
            INNER JOIN milestones m
                ON sc.current_play_count < m.milestone
        )
        SELECT
            c.artist_name AS artistName,
            c.song_name AS songName,
            c.album_name AS albumName,
            c.image_url AS imageUrl,
            c.current_play_count AS currentPlayCount,
            c.milestone AS nextMilestone,
            c.plays_needed AS playsNeeded,
            c.recent_play_count AS recentPlayCount
        FROM candidates c
        WHERE c.rn = 1
          AND (
                c.plays_needed <= :maxPlaysNeeded
                AND c.current_play_count >= (c.milestone * :minPercent / 100.0)
              )
        ORDER BY c.milestone DESC, c.plays_needed ASC, c.recent_play_count DESC, c.artist_name, c.song_name
        LIMIT :limitValue
        """, nativeQuery = true)
    List<UpcomingMilestoneProjection> getUpcomingMilestones(@Param("recentDateStart") Date recentDateStart,
                                                            @Param("dateEnd") Date dateEnd,
                                                            @Param("maxPlaysNeeded") Integer maxPlaysNeeded,
                                                            @Param("minPercent") Integer minPercent,
                                                            @Param("limitValue") Integer limitValue);

    @Query(value = """
        WITH milestones AS (
            SELECT 5 AS milestone
            UNION ALL SELECT 10
            UNION ALL SELECT 20
            UNION ALL SELECT 30
            UNION ALL SELECT 50
            UNION ALL SELECT 75
            UNION ALL SELECT 100
            UNION ALL SELECT 150
            UNION ALL SELECT 200
            UNION ALL SELECT 250
            UNION ALL SELECT 300
        ),
        song_current AS (
            SELECT
                MIN(pe.artist_name) AS artist_name,
                MIN(pe.song_name) AS song_name,
                MIN(pe.album_name) AS album_name,
                MAX(pe.image_url) AS image_url,
                MAX(pe.nb_played) AS current_play_count,
                COUNT(*) FILTER (
                    WHERE pe.date_played >= CURRENT_DATE - interval '90 day'
                ) AS recent_play_count
            FROM play_entry pe
            LEFT JOIN exclusion_artist ex
                ON lower(trim(ex.artist_name)) = lower(trim(pe.artist_name))
            WHERE lower(trim(pe.artist_name)) = lower(trim(:artistName))
              AND lower(trim(pe.song_name)) = lower(trim(:songName))
              AND ex.artist_name IS NULL
        ),
        candidates AS (
            SELECT
                sc.artist_name,
                sc.song_name,
                sc.album_name,
                sc.image_url,
                sc.current_play_count,
                sc.recent_play_count,
                m.milestone,
                (m.milestone - sc.current_play_count) AS plays_needed,
                ROW_NUMBER() OVER (ORDER BY m.milestone) AS rn
            FROM song_current sc
            INNER JOIN milestones m
                ON sc.current_play_count < m.milestone
        )
        SELECT
            c.artist_name AS artistName,
            c.song_name AS songName,
            c.album_name AS albumName,
            c.image_url AS imageUrl,
            c.current_play_count AS currentPlayCount,
            c.milestone AS nextMilestone,
            c.plays_needed AS playsNeeded,
            c.recent_play_count AS recentPlayCount
        FROM candidates c
        WHERE c.rn = 1
        """, nativeQuery = true)
    SongUpcomingMilestoneProjection getUpcomingMilestoneForSong(@Param("artistName") String artistName,
                                                                @Param("songName") String songName);

    // =========================================================
    // SUPPORT
    // =========================================================

    Page<AppleMusicEntry> findByArtistNameNormalizedIsNullAndArtistNameIsNotNull(Pageable pageable);

    long countByArtistNameNormalizedIsNull();

    long countByArtistNameNormalizedIsNotNullAndArtistNameIsNotNull();

    @Modifying
    @Transactional
    @Query("""
        update AppleMusicEntry e
           set e.artistNameNormalized = :canonicalArtistNameNormalized
         where e.artistNameNormalized = :rawArtistNameNormalized
        """)
    int updateArtistNormalizedName(@Param("rawArtistNameNormalized") String rawArtistNameNormalized,
                                   @Param("canonicalArtistNameNormalized") String canonicalArtistNameNormalized);

    interface ArtistSongPlayCountProjection {
        String getSongName();
        Long getPlayCount();
    }

    @Query(value = """
WITH new_artists AS (
    SELECT
        MIN(pe.artist_name) AS artistName,
        COUNT(*)::int AS playCount,
        MIN(pe.date_played) AS firstPlayDate,
        MAX(pe.image_url) AS imageUrl
    FROM play_entry pe
    LEFT JOIN exclusion_artist ex
        ON ex.artist_name_normalized = pe.artist_name_normalized
    WHERE pe.date_played >= :dateStart
      AND pe.date_played < :dateEnd
      AND pe.artist_name_normalized IS NOT NULL
      AND ex.artist_name IS NULL
      AND NOT EXISTS (
          SELECT 1
          FROM play_entry old_pe
          WHERE old_pe.artist_name_normalized = pe.artist_name_normalized
            AND old_pe.date_played < :dateStart
      )
    GROUP BY pe.artist_name_normalized
)
SELECT
    artistName,
    playCount,
    firstPlayDate,
    imageUrl
FROM new_artists
ORDER BY firstPlayDate DESC, artistName
LIMIT :limitValue
""", nativeQuery = true)
    List<RecentNewArtistProjection> getRecentNewArtists(@Param("dateStart") Date dateStart,
                                                        @Param("dateEnd") Date dateEnd,
                                                        @Param("limitValue") Integer limitValue);

    @Query(value = """
    WITH song_rollup AS (
        SELECT
            pe.artist_name_normalized,
            MIN(pe.artist_name) AS artistName,
            lower(trim(pe.song_name)) AS normalized_song_name,
            MAX(pe.nb_played) AS song_play_count
        FROM play_entry pe
        LEFT JOIN exclusion_artist ex
            ON ex.artist_name_normalized = pe.artist_name_normalized
        WHERE ex.artist_name IS NULL
          AND pe.artist_name_normalized IS NOT NULL
        GROUP BY pe.artist_name_normalized, lower(trim(pe.song_name))
    ),
    artist_rollup AS (
        SELECT
            artist_name_normalized,
            MIN(artistName) AS artistName,
            SUM(song_play_count) AS playCount
        FROM song_rollup
        GROUP BY artist_name_normalized
    )
    SELECT
        artistName,
        playCount
    FROM artist_rollup
    WHERE playCount >= :minPlays
    ORDER BY playCount DESC, artistName
    LIMIT :limitValue
    """, nativeQuery = true)
    List<TopArtistProjection> getTopArtistsForRecommendations(@Param("minPlays") Integer minPlays,
                                                              @Param("limitValue") Integer limitValue);


    @Query(value = """
    SELECT
        pe.artist_name AS artistName,
        pe.artist_name_normalized AS artistNameNormalized,
        COUNT(*) AS entryCount,
        MIN(pe.date_played) AS firstPlayedAt,
        MAX(pe.date_played) AS lastPlayedAt,
        MAX(pe.image_url) AS imageUrl
    FROM play_entry pe
    WHERE lower(pe.artist_name) LIKE lower(concat('%', :query, '%'))
    GROUP BY pe.artist_name, pe.artist_name_normalized
    ORDER BY COUNT(*) DESC, MAX(pe.date_played) DESC, pe.artist_name
    LIMIT :limitValue
    """, nativeQuery = true)
    List<ArtistVariantSearchProjection> searchArtistVariants(@Param("query") String query,
                                                             @Param("limitValue") Integer limitValue);

    @Query(value = """
    WITH matching_canonicals AS (
        SELECT DISTINCT
            r.canonical_artist_name,
            r.canonical_artist_name_normalized
        FROM artist_normalization_rule r
        WHERE lower(r.canonical_artist_name) LIKE lower(concat('%', :query, '%'))
    ),
    raw_song_rollup AS (
        SELECT
            pe.artist_name,
            pe.artist_name_normalized,
            lower(trim(pe.song_name)) AS normalized_song_name,
            MAX(pe.nb_played) AS song_play_count,
            MIN(pe.date_played) AS first_played_at,
            MAX(pe.date_played) AS last_played_at,
            MAX(pe.image_url) AS image_url
        FROM play_entry pe
        WHERE lower(pe.artist_name) LIKE lower(concat('%', :query, '%'))
        GROUP BY pe.artist_name, pe.artist_name_normalized, lower(trim(pe.song_name))
    ),
    raw_matches AS (
        SELECT
            rsr.artist_name,
            rsr.artist_name_normalized,
            SUM(rsr.song_play_count) AS raw_play_count,
            MIN(rsr.first_played_at) AS first_played_at,
            MAX(rsr.last_played_at) AS last_played_at,
            MAX(rsr.image_url) AS image_url
        FROM raw_song_rollup rsr
        GROUP BY rsr.artist_name, rsr.artist_name_normalized
    ),
    canonical_song_rollup AS (
        SELECT
            pe.artist_name_normalized,
            lower(trim(pe.song_name)) AS normalized_song_name,
            MAX(pe.nb_played) AS song_play_count
        FROM play_entry pe
        WHERE pe.artist_name_normalized IN (
            SELECT mc.canonical_artist_name_normalized
            FROM matching_canonicals mc
        )
        GROUP BY pe.artist_name_normalized, lower(trim(pe.song_name))
    ),
    canonical_totals AS (
        SELECT
            csr.artist_name_normalized,
            SUM(csr.song_play_count) AS canonical_play_count
        FROM canonical_song_rollup csr
        GROUP BY csr.artist_name_normalized
    )
    SELECT
        rm.artist_name AS artistName,
        rm.artist_name_normalized AS artistNameNormalized,
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM matching_canonicals mc
                WHERE rm.artist_name_normalized = mc.canonical_artist_name_normalized
                  AND lower(rm.artist_name) = lower(mc.canonical_artist_name)
            )
            THEN COALESCE(ct.canonical_play_count, rm.raw_play_count)
            ELSE rm.raw_play_count
        END AS entryCount,
        rm.first_played_at AS firstPlayedAt,
        rm.last_played_at AS lastPlayedAt,
        rm.image_url AS imageUrl
    FROM raw_matches rm
    LEFT JOIN canonical_totals ct
        ON ct.artist_name_normalized = rm.artist_name_normalized
    WHERE NOT EXISTS (
        SELECT 1
        FROM matching_canonicals mc
        WHERE rm.artist_name_normalized = mc.canonical_artist_name_normalized
          AND lower(rm.artist_name) <> lower(mc.canonical_artist_name)
    )
    ORDER BY
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM matching_canonicals mc
                WHERE lower(rm.artist_name) = lower(mc.canonical_artist_name)
            ) THEN 0
            ELSE 1
        END,
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM matching_canonicals mc
                WHERE rm.artist_name_normalized = mc.canonical_artist_name_normalized
                  AND lower(rm.artist_name) = lower(mc.canonical_artist_name)
            )
            THEN COALESCE(ct.canonical_play_count, rm.raw_play_count)
            ELSE rm.raw_play_count
        END DESC,
        rm.last_played_at DESC,
        rm.artist_name
    LIMIT :limitValue
    """, nativeQuery = true)
    List<ArtistVariantSearchProjection> searchArtistVariantsKeepingCanonical(@Param("query") String query,
                                                                             @Param("limitValue") Integer limitValue);


    @Query(value = """
    SELECT
        COALESCE(
            NULLIF(LOWER(TRIM(pe.artist_name_normalized)), ''),
            LOWER(TRIM(pe.artist_name))
        ) AS "artistKey",
        COUNT(*) AS "playCount"
    FROM play_entry pe
    WHERE pe.date_played >= :yearStart
      AND pe.date_played < :dayStart
    GROUP BY COALESCE(
        NULLIF(LOWER(TRIM(pe.artist_name_normalized)), ''),
        LOWER(TRIM(pe.artist_name))
    )
    """, nativeQuery = true)
    List<ArtistYearPlayCountProjection> getArtistPlayCountsBeforeDay(
            @Param("yearStart") Date yearStart,
            @Param("dayStart") Date dayStart
    );
}

