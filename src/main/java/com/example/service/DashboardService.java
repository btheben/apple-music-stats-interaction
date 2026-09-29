package com.example.service;

import com.example.depot.PlayEntryDepot;
import com.example.modele.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final PlayEntryService playEntryService;
    private final PlayEntryDepot playEntryDepot;

    public DashboardService(PlayEntryService playEntryService,
                            PlayEntryDepot playEntryDepot) {
        this.playEntryService = playEntryService;
        this.playEntryDepot = playEntryDepot;
    }

    public DashboardCoreDto getDashboardCore(Date dateStart, Date dateEnd) {
        DashboardCoreDto dto = new DashboardCoreDto();

        List<TopArtist> rawTopArtists = safeList(playEntryService.getTopArtistDateInterval(dateStart, dateEnd, 20));
        dto.setTopArtists(buildDashboardTopArtists(rawTopArtists));

        dto.setTopSongs(safeList(playEntryService.getTopSongDateInterval(dateStart, dateEnd, 5)));

        dto.setRecentActivity(
                safeList(playEntryService.getByDatePlayed(dateStart, dateEnd))
                        .stream()
                        .limit(8)
                        .collect(Collectors.toList())
        );

        PeriodRange currentYearRange = getCurrentYearToDateRange();

        HIndexSummaryProjection summary = playEntryService.getArtistHIndexSummary(
                currentYearRange.getStart(),
                currentYearRange.getEnd()
        );

        DashboardHIndexSummaryDto summaryDto;
        if (summary == null) {
            summaryDto = new DashboardHIndexSummaryDto(0, 0);
        } else {
            summaryDto = new DashboardHIndexSummaryDto(
                    summary.getHIndex() != null ? summary.getHIndex() : 0,
                    summary.getArtistsNeededForNextH() != null ? summary.getArtistsNeededForNextH() : 0
            );
        }

        dto.setHIndexSummary(summaryDto);

        List<HIndexArtistProjection> details = safeList(
                playEntryService.getArtistHIndexDetails(
                        currentYearRange.getStart(),
                        currentYearRange.getEnd()
                )
        );

        dto.setHIndexTargets(
                details.stream()
                        .filter(item -> item.getPlaysNeededForNextH() != null && item.getPlaysNeededForNextH() > 0)
                        .sorted(Comparator
                                .comparing(HIndexArtistProjection::getPlaysNeededForNextH)
                                .thenComparing(HIndexArtistProjection::getPlayCount, Comparator.reverseOrder()))
                        .limit(3)
                        .collect(Collectors.toList())
        );

        try {
            dto.setRecentMilestones(
                    safeList(playEntryService.getRecentMilestones(dateStart, dateEnd))
                            .stream()
                            .sorted(Comparator.comparing(
                                    RecentMilestoneProjection::getReachedAt,
                                    Comparator.nullsLast(Comparator.reverseOrder())
                            ))
                            .limit(6)
                            .collect(Collectors.toList())
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        List<RecentRediscoveryProjection> rediscoveries = safeList(
                playEntryService.getRecentRediscoveries(
                        dateStart,
                        dateEnd,
                        365,
                        10,
                        1
                )
        );

        System.out.println("top artists size = " + rawTopArtists.size());
        System.out.println("top songs size = " + dto.getTopSongs().size());
        System.out.println("recent activity size = " + dto.getRecentActivity().size());
        System.out.println("hindex details size = " + details.size());
        System.out.println("recent milestones size = " + dto.getRecentMilestones().size());
        dto.setLatestRediscovery(rediscoveries.isEmpty() ? null : rediscoveries.get(0));

        dto.setTrendingArtists(getTrendingArtists(dateStart, dateEnd, 6));

        return dto;
    }

    private List<DashboardTopArtistDto> buildDashboardTopArtists(List<TopArtist> artists) {
        return artists.stream()
                .map(artist -> new DashboardTopArtistDto(
                        artist.getArtistName(),
                        artist.getNbPlayed(),
                        findArtistImageUrl(artist.getArtistName())
                ))
                .collect(Collectors.toList());
    }

    private String findArtistImageUrl(String artistName) {
        if (artistName == null || artistName.isBlank()) {
            return null;
        }

        List<AppleMusicEntry> entries = safeList(
                playEntryDepot.findByArtistNameIgnoreCaseOrderByDatePlayedDesc(artistName)
        );

        return entries.stream()
                .map(AppleMusicEntry::getImageUrl)
                .filter(Objects::nonNull)
                .filter(url -> !url.isBlank())
                .findFirst()
                .orElse(null);
    }

    public DashboardDiscoveryDto getDashboardDiscovery(Date dateStart, Date dateEnd) {
        DashboardDiscoveryDto dto = new DashboardDiscoveryDto();

        List<RediscoveryArtistProjection> rediscoveryArtists =
                safeList(playEntryService.getRandomRediscoveryArtists(50, 6));

        dto.setRediscoveryArtists(rediscoveryArtists);

        dto.setNewArtists(
                safeList(playEntryDepot.getRecentNewArtists(dateStart, dateEnd, 3))
        );

        dto.setForgottenArtist(buildForgottenArtist(rediscoveryArtists));

        return dto;
    }

    private ForgottenArtistDto buildForgottenArtist(List<RediscoveryArtistProjection> rediscoveryArtists) {
        if (rediscoveryArtists == null || rediscoveryArtists.isEmpty()) {
            return null;
        }

        RediscoveryArtistProjection item = rediscoveryArtists.get(0);
        String imageUrl = findArtistImageUrl(item.getArtistName());

        return new ForgottenArtistDto(
                item.getArtistName(),
                item.getTotalPlays() != null ? item.getTotalPlays().intValue() : 0,
                item.getLastPlayed(),
                item.getDaysSinceLastPlay(),
                imageUrl
        );
    }

    private PeriodRange getCurrentYearToDateRange() {
        Calendar cal = Calendar.getInstance();

        Date end = new Date();

        cal.setTime(end);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date start = cal.getTime();

        return new PeriodRange(start, end);
    }

    private List<TrendingArtistDto> getTrendingArtists(Date currentStart, Date currentEnd, int limit) {
        PeriodRange previousRange = getPreviousPeriod(currentStart, currentEnd);

        List<TopArtist> currentArtists = safeList(
                playEntryService.getTopArtistDateInterval(currentStart, currentEnd, 30)
        );

        List<TopArtist> previousArtists = safeList(
                playEntryService.getTopArtistDateInterval(previousRange.getStart(), previousRange.getEnd(), 30)
        );

        Map<String, TopArtist> previousMap = previousArtists.stream()
                .filter(a -> a.getArtistName() != null)
                .collect(Collectors.toMap(
                        a -> normalize(a.getArtistName()),
                        Function.identity(),
                        (a, b) -> a
                ));

        return currentArtists.stream()
                .filter(a -> a.getArtistName() != null)
                .map(current -> {
                    TopArtist previous = previousMap.get(normalize(current.getArtistName()));

                    int currentPlays = safeInt(current.getNbPlayed());
                    int previousPlays = previous != null ? safeInt(previous.getNbPlayed()) : 0;
                    int delta = currentPlays - previousPlays;

                    Integer percentChange;
                    if (previousPlays <= 0) {
                        percentChange = currentPlays > 0 ? 100 : 0;
                    } else {
                        percentChange = (int) Math.round(((currentPlays - previousPlays) * 100.0) / previousPlays);
                    }

                    return new TrendingArtistDto(
                            current.getArtistName(),
                            currentPlays,
                            previousPlays,
                            delta,
                            percentChange
                    );
                })
                .filter(item -> item.getCurrentPlays() != null && item.getCurrentPlays() > 0)
                .sorted(Comparator
                        .comparing(TrendingArtistDto::getDelta, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TrendingArtistDto::getCurrentPlays, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private PeriodRange getPreviousPeriod(Date currentStart, Date currentEnd) {
        long durationMs = currentEnd.getTime() - currentStart.getTime();
        long oneDayMs = 24L * 60 * 60 * 1000;

        if (durationMs < oneDayMs) {
            durationMs = 7L * oneDayMs;
        }

        Date previousEnd = new Date(currentStart.getTime() - 1);
        Date previousStart = new Date(previousEnd.getTime() - durationMs);

        return new PeriodRange(previousStart, previousEnd);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private int safeInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private static class PeriodRange {
        private final Date start;
        private final Date end;

        public PeriodRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }
    }
}