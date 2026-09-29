package com.example.scheduler;

import com.example.service.PlayEntryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlayEntryImportScheduler {

    private final PlayEntryService playEntryService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public PlayEntryImportScheduler(PlayEntryService playEntryService) {
        this.playEntryService = playEntryService;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void importPlayLogs() {
        if (!running.compareAndSet(false, true)) {
            System.out.println("Import déjà en cours, on saute cette exécution.");
            return;
        }

        try {
            int nbFiles = playEntryService.getNbFiles();

            if (nbFiles <= 0) {
                System.out.println("[" + LocalDateTime.now() + "] Aucun fichier à importer.");
                return;
            }

            System.out.println("[" + LocalDateTime.now() + "] " + nbFiles + " fichier(s) détecté(s). Lancement de l'import...");
            playEntryService.loadFiles();

        } catch (Exception e) {
            System.out.println("Erreur dans le scheduler d'import: " + e.getMessage());
            e.printStackTrace();
        } finally {
            running.set(false);
        }
    }
}