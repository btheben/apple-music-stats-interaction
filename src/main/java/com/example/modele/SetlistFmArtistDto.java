package com.example.modele;

public class SetlistFmArtistDto {

    private String mbid;
    private String name;
    private String sortName;
    private String disambiguation;
    private String url;

    public SetlistFmArtistDto() {
    }

    public SetlistFmArtistDto(String mbid, String name, String sortName, String disambiguation, String url) {
        this.mbid = mbid;
        this.name = name;
        this.sortName = sortName;
        this.disambiguation = disambiguation;
        this.url = url;
    }

    public String getMbid() {
        return mbid;
    }

    public String getName() {
        return name;
    }

    public String getSortName() {
        return sortName;
    }

    public String getDisambiguation() {
        return disambiguation;
    }

    public String getUrl() {
        return url;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public void setDisambiguation(String disambiguation) {
        this.disambiguation = disambiguation;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}