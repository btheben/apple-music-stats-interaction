package com.example.modele;
public class SimilarArtistDto {
    private String name;
    private String match;
    private String url;
    private String imageUrl;
    private boolean presentInLibrary;
    private int localPlayCount;

    public SimilarArtistDto() {
    }

    public SimilarArtistDto(String name, String match, String url, String imageUrl,
                            boolean presentInLibrary, int localPlayCount) {
        this.name = name;
        this.match = match;
        this.url = url;
        this.imageUrl = imageUrl;
        this.presentInLibrary = presentInLibrary;
        this.localPlayCount = localPlayCount;
    }

    public String getName() { return name; }
    public String getMatch() { return match; }
    public String getUrl() { return url; }
    public String getImageUrl() { return imageUrl; }
    public boolean isPresentInLibrary() { return presentInLibrary; }
    public int getLocalPlayCount() { return localPlayCount; }

    public void setName(String name) { this.name = name; }
    public void setMatch(String match) { this.match = match; }
    public void setUrl(String url) { this.url = url; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPresentInLibrary(boolean presentInLibrary) { this.presentInLibrary = presentInLibrary; }
    public void setLocalPlayCount(int localPlayCount) { this.localPlayCount = localPlayCount; }
}