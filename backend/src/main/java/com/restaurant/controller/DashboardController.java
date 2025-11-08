package com.restaurant.controller;

import com.restaurant.common.Result;
import com.restaurant.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }

    @GetMapping("/level-distribution")
    public Result<Map<String, Object>> getLevelDistribution() {
        return Result.success(dashboardService.getLevelDistribution());
    }
}