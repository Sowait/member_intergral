package com.restaurant.service;

import java.util.Map;

public interface DashboardService {
    Map<String, Object> getStatistics();
    Map<String, Object> getLevelDistribution();
}