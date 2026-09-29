package com.example.controller;

import com.example.modele.*;
import com.example.service.PlayEntryService;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/stats")
public class StatsController extends BaseDateController {

    private final PlayEntryService playEntryService;

    public StatsController(PlayEntryService playEntryService) {
        this.playEntryService = playEntryService;
    }

    // =========================
    // Nouveaux endpoints
    // =========================

    @GetMapping("/songs/top")
    public List<TopSong> getTopSongs(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        int safeLimit = limitValue != null ? limitValue : 50;

        return playEntryService.getTopSongDateInterval(range.getDateStart(), range.getDateEnd(), safeLimit);
    }

    @GetMapping("/albums/top")
    public List<TopAlbum> getTopAlbums(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        int safeLimit = limitValue != null ? limitValue : 50;

        return playEntryService.getTopAlbumDateInterval(range.getDateStart(), range.getDateEnd(), safeLimit);
    }

    @GetMapping("/daily")
    public List<DailyPlayCountProjection> getDailyPlayCount(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, false);
        return playEntryService.getDailyPlayCountBetween(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/h-index/summary")
    public HIndexSummaryProjection getHIndexSummary(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getArtistHIndexSummary(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/h-index/details")
    public List<HIndexArtistProjection> getHIndexDetails(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getArtistHIndexDetails(range.getDateStart(), range.getDateEnd());
    }

    // =========================
    // Routes legacy - By Month
    // =========================

    @GetMapping("/topArtistByMonth/{year}/{month}")
    public List<TopArtist> getTopArtistByMonth(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month) {

        Date start = startOfMonth(year, month);
        Date end = startOfNextMonth(year, month);

        return playEntryService.getTopArtistDateInterval(start, end);
    }

    @GetMapping("/topSongByMonth/{year}/{month}")
    public List<TopSong> getTopSongByMonth(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month) {

        Date start = startOfMonth(year, month);
        Date end = startOfNextMonth(year, month);

        return playEntryService.getTopSongDateInterval(start, end);
    }

    @GetMapping("/topAlbumByMonth/{year}/{month}")
    public List<TopAlbum> getTopAlbumByMonth(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month) {

        Date start = startOfMonth(year, month);
        Date end = startOfNextMonth(year, month);

        return playEntryService.getTopAlbumDateInterval(start, end);
    }

    // =========================
    // Routes legacy - By Year
    // =========================

    @GetMapping("/topArtistByYear/{year}")
    public List<TopArtist> getTopArtistByYear(
            @PathVariable("year") Integer year) {

        Date start = startOfYear(year);
        Date end = startOfNextYear(year);

        return playEntryService.getTopArtistDateInterval(start, end);
    }

    @GetMapping("/topSongByYear/{year}")
    public List<TopSong> getTopSongByYear(
            @PathVariable("year") Integer year) {

        Date start = startOfYear(year);
        Date end = startOfNextYear(year);

        return playEntryService.getTopSongDateInterval(start, end);
    }

    @GetMapping("/topAlbumByYear/{year}")
    public List<TopAlbum> getTopAlbumByYear(
            @PathVariable("year") Integer year) {

        Date start = startOfYear(year);
        Date end = startOfNextYear(year);

        return playEntryService.getTopAlbumDateInterval(start, end);
    }

    // =========================
    // Routes legacy - Last Week
    // =========================

    @GetMapping("/topArtistLastWeek")
    public List<TopArtist> getTopArtistLastWeek() {
        return playEntryService.getTopArtistLastWeek(new Date());
    }

    @GetMapping("/topSongLastWeek")
    public List<TopSong> getTopSongLastWeek() {
        return playEntryService.getTopSongLastWeek(new Date());
    }

    // =========================
    // Routes legacy - Counts / series
    // =========================

    @GetMapping("/top10Days")
    public List<PeriodPlayCountProjection> getTop10Days(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getTop10Days(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/top10Months")
    public List<PeriodPlayCountProjection> getTop10Months(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getTop10Months(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/top10Years")
    public List<PeriodPlayCountProjection> getTop10Years(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getTop10Years(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/monthlyPlayCount")
    public List<PeriodPlayCountProjection> getMonthlyPlayCount(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getMonthlyPlayCountBetween(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/yearlyPlayCount")
    public List<PeriodPlayCountProjection> getYearlyPlayCount(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getYearlyPlayCountBetween(range.getDateStart(), range.getDateEnd());
    }

    // =========================
    // Routes legacy - Discovery / counts
    // =========================

    @GetMapping("/topNewArtists")
    public List<NewArtistProjection> getTopNewArtists(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        int safeLimit = limitValue != null ? limitValue : 10;

        return playEntryService.getTopNewArtists(range.getDateStart(), range.getDateEnd(), safeLimit);
    }

    @GetMapping("/newArtistCount")
    public Long getNewArtistCount(
            @RequestParam("dateStart") String dateStartString,
            @RequestParam("dateEnd") String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getNewArtistCount(range.getDateStart(), range.getDateEnd());
    }

    // =========================
    // Helpers
    // =========================

    private Date startOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(calendar);
        return calendar.getTime();
    }

    private Date startOfNextMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(calendar);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private Date startOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(calendar);
        return calendar.getTime();
    }

    private Date startOfNextYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year + 1);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        resetTime(calendar);
        return calendar.getTime();
    }

    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @GetMapping("/datePlayed/{date}")
    public List<AppleMusicEntry> getEntriesByDate(
            @PathVariable("date") String date) throws Exception {

        Date parsedDate = parseDateFromRoute(date);
        Date nextDay = addOneDay(parsedDate);

        return playEntryService.getByDatePlayed(parsedDate, nextDay);
    }

    private Date parseDateFromRoute(String value) {
        String[] parts = value.split("-");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Format de date invalide. Attendu: dd-MM-yyyy");
        }

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int year = Integer.parseInt(parts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    @DeleteMapping("/deleteEntry/{id}")
    public void deleteEntry(@PathVariable("id") long id) {
        playEntryService.deleteEntry(id);
    }

    @PostMapping("/updateEntry/{id}")
    public void updateEntry(@PathVariable("id") long id,
                            @RequestParam("nbPlayed") int nbPlayed,
                            @RequestParam("albumName") String albumName,
                            @RequestParam("songName") String songName) {

        AppleMusicEntry entry = playEntryService.findEntryById(id); // ou équivalent
        entry.setNbPlayed(nbPlayed);
        entry.setAlbumName(albumName);
        entry.setSongName(songName);

        playEntryService.updateEntry(entry);
    }

    @GetMapping("/datePlayed/{date}/summary")
    public DailySummaryDto getDailySummary(@PathVariable("date") String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.CANADA_FRENCH);
        sdf.setLenient(false);

        Date start = sdf.parse(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return playEntryService.getDailySummary(start, calendar.getTime());
    }

    @GetMapping("/h-index/history")
    public List<HIndexHistoryDto> getHIndexHistory(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getArtistHIndexHistory(range.getDateStart(), range.getDateEnd());
    }
}