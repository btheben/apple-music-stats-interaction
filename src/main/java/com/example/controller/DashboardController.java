package com.example.controller;

import com.example.modele.DashboardCoreDto;
import com.example.modele.DashboardDiscoveryDto;
import com.example.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseDateController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/core")
    public DashboardCoreDto getDashboardCore(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return dashboardService.getDashboardCore(range.getDateStart(), range.getDateEnd());
    }

    @GetMapping("/discovery")
    public DashboardDiscoveryDto getDashboardDiscovery(
            @RequestParam(value = "dateStart", required = false) String dateStartString,
            @RequestParam(value = "dateEnd", required = false) String dateEndString) throws Exception {

        DateRange range = resolveDateRange(dateStartString, dateEndString, true);
        return dashboardService.getDashboardDiscovery(range.getDateStart(), range.getDateEnd());
    }
}