package com.example.modele;

public class ArtistNormalizationRuleDto {
    private String rawArtistName;
    private String rawArtistNameNormalized;
    private String canonicalArtistName;
    private String canonicalArtistNameNormalized;
    private boolean exists;

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

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}