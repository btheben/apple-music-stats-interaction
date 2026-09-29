package com.example.controller;

import com.example.modele.RecentMilestoneProjection;
import com.example.modele.UpcomingMilestoneProjection;
import com.example.service.PlayEntryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/milestones")
public class MilestoneController extends BaseDateController {

    private final PlayEntryService playEntryService;

    public MilestoneController(PlayEntryService playEntryService) {
        this.playEntryService = playEntryService;
    }

    @GetMapping("/recent")
    public List<RecentMilestoneProjection> getRecentMilestones(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return playEntryService.getRecentMilestones(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/upcoming")
    public List<UpcomingMilestoneProjection> getUpcomingMilestones(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString,
            @RequestParam(value = "maxPlaysNeeded", required = false) Integer maxPlaysNeeded,
            @RequestParam(value = "minPercent", required = false) Integer minPercent,
            @RequestParam(value = "limitValue", required = false) Integer limitValue) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);

        return playEntryService.getUpcomingMilestones(
                range.getDateStart(),
                range.getDateEnd(),
                maxPlaysNeeded != null ? maxPlaysNeeded : 10,
                minPercent != null ? minPercent : 70,
                limitValue != null ? limitValue : 20
        );
    }
}
