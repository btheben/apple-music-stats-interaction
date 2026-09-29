package com.example.modele;

public class ArtistRecommendationDto {
    private String sourceArtistName;
    private String recommendedArtistName;
    private String recommendedArtistUrl;
    private String imageUrl;
    private String similarityScore;
    private boolean alreadyInLibrary;
    private int localPlayCount;

    public ArtistRecommendationDto() {
    }

    public ArtistRecommendationDto(String sourceArtistName,
                                   String recommendedArtistName,
                                   String recommendedArtistUrl,
                                   String imageUrl,
                                   String similarityScore,
                                   boolean alreadyInLibrary,
                                   int localPlayCount) {
        this.sourceArtistName = sourceArtistName;
        this.recommendedArtistName = recommendedArtistName;
        this.recommendedArtistUrl = recommendedArtistUrl;
        this.imageUrl = imageUrl;
        this.similarityScore = similarityScore;
        this.alreadyInLibrary = alreadyInLibrary;
        this.localPlayCount = localPlayCount;
    }

    public String getSourceArtistName() { return sourceArtistName; }
    public String getRecommendedArtistName() { return recommendedArtistName; }
    public String getRecommendedArtistUrl() { return recommendedArtistUrl; }
    public String getImageUrl() { return imageUrl; }
    public String getSimilarityScore() { return similarityScore; }
    public boolean isAlreadyInLibrary() { return alreadyInLibrary; }
    public int getLocalPlayCount() { return localPlayCount; }

    public void setSourceArtistName(String sourceArtistName) { this.sourceArtistName = sourceArtistName; }
    public void setRecommendedArtistName(String recommendedArtistName) { this.recommendedArtistName = recommendedArtistName; }
    public void setRecommendedArtistUrl(String recommendedArtistUrl) { this.recommendedArtistUrl = recommendedArtistUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSimilarityScore(String similarityScore) { this.similarityScore = similarityScore; }
    public void setAlreadyInLibrary(boolean alreadyInLibrary) { this.alreadyInLibrary = alreadyInLibrary; }
    public void setLocalPlayCount(int localPlayCount) { this.localPlayCount = localPlayCount; }
}
