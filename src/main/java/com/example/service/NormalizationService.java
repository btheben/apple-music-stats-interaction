package com.example.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class NormalizationService {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUM_BASIC = Pattern.compile("[^a-z0-9&+ ]");

    public String normalizeArtistName(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String original = raw.trim();

        String value = original
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('`', '\'')
                .replace('–', '-')
                .replace('—', '-');

        value = Normalizer.normalize(value, Normalizer.Form.NFD);
        value = value.replaceAll("\\p{M}", "");
        value = value.toLowerCase(Locale.ROOT);
        value = value.replace("&", " and ");
        value = NON_ALPHANUM_BASIC.matcher(value).replaceAll(" ");
        value = MULTIPLE_SPACES.matcher(value).replaceAll(" ").trim();

        if (!value.isBlank()) {
            return value;
        }

        // Fallback pour les artistes composés de symboles ou caractères non ASCII
        String fallback = original
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('`', '\'')
                .replace('–', '-')
                .replace('—', '-')
                .toLowerCase(Locale.ROOT);

        fallback = MULTIPLE_SPACES.matcher(fallback).replaceAll(" ").trim();

        return fallback.isBlank() ? null : fallback;
    }
}