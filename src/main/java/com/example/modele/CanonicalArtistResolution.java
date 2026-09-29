package com.example.modele;

public class CanonicalArtistResolution {

    private String requestedArtistName;
    private String requestedArtistNameNormalized;
    private String canonicalArtistName;
    private String canonicalArtistNameNormalized;

    public String getRequestedArtistName() {
        return requestedArtistName;
    }

    public void setRequestedArtistName(String requestedArtistName) {
        this.requestedArtistName = requestedArtistName;
    }

    public String getRequestedArtistNameNormalized() {
        return requestedArtistNameNormalized;
    }

    public void setRequestedArtistNameNormalized(String requestedArtistNameNormalized) {
        this.requestedArtistNameNormalized = requestedArtistNameNormalized;
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
}