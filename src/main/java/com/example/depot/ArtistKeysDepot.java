package com.example.depot;

import com.example.modele.ArtistKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistKeysDepot extends JpaRepository<ArtistKeys, Long> {

       ArtistKeys findFirstByArtistNameIgnoreCase(String artistName);
}