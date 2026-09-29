package com.example.modele;

import java.util.List;

public class DashboardDiscoveryDto {

    private List<RediscoveryArtistProjection> rediscoveryArtists;
    private List<RecentNewArtistProjection> newArtists;
    private ForgottenArtistDto forgottenArtist;

    public List<RediscoveryArtistProjection> getRediscoveryArtists() {
        return rediscoveryArtists;
    }

    public void setRediscoveryArtists(List<RediscoveryArtistProjection> rediscoveryArtists) {
        this.rediscoveryArtists = rediscoveryArtists;
    }

    public List<RecentNewArtistProjection> getNewArtists() {
        return newArtists;
    }

    public void setNewArtists(List<RecentNewArtistProjection> newArtists) {
        this.newArtists = newArtists;
    }

    public ForgottenArtistDto getForgottenArtist() {
        return forgottenArtist;
    }

    public void setForgottenArtist(ForgottenArtistDto forgottenArtist) {
        this.forgottenArtist = forgottenArtist;
    }
}