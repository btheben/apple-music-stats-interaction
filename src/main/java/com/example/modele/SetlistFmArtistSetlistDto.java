package com.example.modele;

import java.util.ArrayList;
import java.util.List;

public class SetlistFmArtistSetlistDto {

    private String setlistId;
    private String versionId;
    private String eventDate;
    private String venueName;
    private String cityName;
    private String countryName;
    private String tourName;
    private String url;
    private List<String> songs = new ArrayList<>();

    public SetlistFmArtistSetlistDto() {
    }

    public String getSetlistId() {
        return setlistId;
    }

    public void setSetlistId(String setlistId) {
        this.setlistId = setlistId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getSongs() {
        return songs;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }
}