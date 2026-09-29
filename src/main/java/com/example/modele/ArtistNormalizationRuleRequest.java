package com.example.modele;

public class ArtistNormalizationRuleRequest {

    private String rawArtistName;
    private String canonicalArtistName;

    public String getRawArtistName() {
        return rawArtistName;
    }

    public void setRawArtistName(String rawArtistName) {
        this.rawArtistName = rawArtistName;
    }

    public String getCanonicalArtistName() {
        return canonicalArtistName;
    }

    public void setCanonicalArtistName(String canonicalArtistName) {
        this.canonicalArtistName = canonicalArtistName;
    }
}