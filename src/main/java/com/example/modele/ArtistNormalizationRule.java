package com.example.modele;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "artist_normalization_rule")
public class ArtistNormalizationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_artist_name", nullable = false)
    private String rawArtistName;

    @Column(name = "raw_artist_name_normalized", nullable = false)
    private String rawArtistNameNormalized;

    @Column(name = "canonical_artist_name", nullable = false)
    private String canonicalArtistName;

    @Column(name = "canonical_artist_name_normalized", nullable = false)
    private String canonicalArtistNameNormalized;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public String getRawArtistName() {
        return rawArtistName;
    }

    public void setRawArtistName(String rawArtistName) {
        this.rawArtistName = rawArtistName;
    }

    public String getRawArtistNameNormalized() {
        return rawArtistNameNormalized;
    }

    public void setRawArtistNameNormalized(String rawArtistNameNormalized) {
        this.rawArtistNameNormalized = rawArtistNameNormalized;
    }

    public String getCanonicalArtistName() {
        return canonicalArtistName;
    }

    public void setCanonicalArtistName(String canonicalArtistName) {
        this.canonicalArtistName = canonicalArtistName;
    }

    public String getCanonicalArtistNameNormalized() {
        return canonicalArtistNameNormalized;
    }

    public void setCanonicalArtistNameNormalized(String canonicalArtistNameNormalized) {
        this.canonicalArtistNameNormalized = canonicalArtistNameNormalized;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}