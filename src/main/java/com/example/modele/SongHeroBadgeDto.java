package com.example.modele;

public class SongHeroBadgeDto {

    private String label;
    private String type;
    private String source;

    public SongHeroBadgeDto() {
    }

    public SongHeroBadgeDto(String label, String type, String source) {
        this.label = label;
        this.type = type;
        this.source = source;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSource(String source) {
        this.source = source;
    }
}