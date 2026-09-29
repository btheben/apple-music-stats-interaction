package com.example.modele;
public class RecommendedTrackDto {
    private String name;
    private String url;
    private int localPlayCount;
    private boolean alreadyKnown;
    private Integer lastFmRank;

    public RecommendedTrackDto() {
    }

    public RecommendedTrackDto(String name, String url, int localPlayCount, boolean alreadyKnown, Integer lastFmRank) {
        this.name = name;
        this.url = url;
        this.localPlayCount = localPlayCount;
        this.alreadyKnown = alreadyKnown;
        this.lastFmRank = lastFmRank;
    }

    public String getName() { return name; }
    public String getUrl() { return url; }
    public int getLocalPlayCount() { return localPlayCount; }
    public boolean isAlreadyKnown() { return alreadyKnown; }
    public Integer getLastFmRank() { return lastFmRank; }

    public void setName(String name) { this.name = name; }
    public void setUrl(String url) { this.url = url; }
    public void setLocalPlayCount(int localPlayCount) { this.localPlayCount = localPlayCount; }
    public void setAlreadyKnown(boolean alreadyKnown) { this.alreadyKnown = alreadyKnown; }
    public void setLastFmRank(Integer lastFmRank) { this.lastFmRank = lastFmRank; }
}