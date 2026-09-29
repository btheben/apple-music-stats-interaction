package com.example.depot;

import com.example.modele.ArtistKeys;
import com.example.modele.TopArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopArtistDepot extends JpaRepository<TopArtist, String> {
       @Query (value="with top as (select artist_name, song_name, album_name, Max(nb_played) number_played from play_entry pe where artist_name not in (select artist_name a from exclusion_artist) group by artist_name, song_name, album_name order by number_played desc),\n" +
               "top2 as (select artist_name, sum(number_played) nb_played from top group by artist_name having sum(number_played) >= 97 order by nb_played desc),\n" +
               "top3 as (select distinct on (artist_name) artist_name, song_name, max(date_played) last_played from play_entry pe2 group by artist_name, song_name order by artist_name, last_played desc)\n" +
               "select t2.artist_name, t2.nb_played, t3.song_name, t3.last_played from top2 t2, top3 t3 where t2.artist_name = t3.artist_name order by t2.nb_played desc", nativeQuery=true)
       List<TopArtist> getTopArtistAllTime();

       @Query (value="with top as (select song_name, Max(nb_played) nb_played from play_entry pe where artist_name = :artistName group by song_name)\n" +
               "select sum(nb_played) as nb_played from top", nativeQuery=true)
       
       Integer getTotalByArtist(@Param("artistName") String artistName);

       @Query (value="with top as (select artist_name, song_name, Max(nb_played) number_played from play_entry pe where artist_name not in (select artist_name a from exclusion_artist) group by artist_name, song_name order by number_played desc),\n" +
               "     by_artist as (select artist_name, sum(number_played) as nb_played, ROW_NUMBER () OVER (ORDER BY sum(number_played) desc) as artist_position from top group by artist_name)\n" +
               "     select artist_position from by_artist where artist_name = :artistName", nativeQuery=true)
       Integer getPositionByArtist(@Param("artistName") String artistName);

       @Query(value = """
    with top as (
        select
            lower(trim(pe.song_name)) as normalized_song_name,
            max(pe.nb_played) as nb_played
        from play_entry pe
        left join exclusion_artist ex
            on ex.artist_name_normalized = pe.artist_name_normalized
        where pe.artist_name_normalized = :artistNameNormalized
          and ex.artist_name is null
        group by lower(trim(pe.song_name))
    )
    select coalesce(sum(nb_played), 0)
    from top
    """, nativeQuery = true)
       Integer getTotalByArtistNormalized(@Param("artistNameNormalized") String artistNameNormalized);


       @Query(value = """
    with top as (
        select
            pe.artist_name_normalized,
            lower(trim(pe.song_name)) as normalized_song_name,
            max(pe.nb_played) as number_played
        from play_entry pe
        left join exclusion_artist ex
            on ex.artist_name_normalized = pe.artist_name_normalized
        where pe.artist_name_normalized is not null
          and ex.artist_name is null
        group by pe.artist_name_normalized, lower(trim(pe.song_name))
    ),
    by_artist as (
        select
            artist_name_normalized,
            sum(number_played) as nb_played,
            row_number() over (order by sum(number_played) desc) as artist_position
        from top
        group by artist_name_normalized
    )
    select artist_position
    from by_artist
    where artist_name_normalized = :artistNameNormalized
    """, nativeQuery = true)
       Integer getPositionByArtistNormalized(@Param("artistNameNormalized") String artistNameNormalized);


       @Query(value = """
    with top as (
        select
            pe.artist_name_normalized,
            min(pe.artist_name) as artist_name,
            lower(trim(pe.song_name)) as normalized_song_name,
            max(pe.nb_played) as number_played
        from play_entry pe
        left join exclusion_artist ex
            on ex.artist_name_normalized = pe.artist_name_normalized
        where pe.artist_name_normalized is not null
          and ex.artist_name is null
        group by pe.artist_name_normalized, lower(trim(pe.song_name))
    ),
    top2 as (
        select
            artist_name_normalized,
            min(artist_name) as artist_name,
            sum(number_played) as nb_played
        from top
        group by artist_name_normalized
        having sum(number_played) >= 97
    ),
    top3 as (
        select distinct on (pe.artist_name_normalized)
            pe.artist_name_normalized,
            pe.song_name,
            pe.date_played as last_played
        from play_entry pe
        left join exclusion_artist ex
            on ex.artist_name_normalized = pe.artist_name_normalized
        where pe.artist_name_normalized is not null
          and ex.artist_name is null
        order by pe.artist_name_normalized, pe.date_played desc
    )
    select
        t2.artist_name,
        t2.nb_played,
        t3.song_name,
        t3.last_played
    from top2 t2
    join top3 t3
      on t2.artist_name_normalized = t3.artist_name_normalized
    order by t2.nb_played desc
    """, nativeQuery = true)
       List<TopArtist> getTopArtistAllTimeNormalized();
}