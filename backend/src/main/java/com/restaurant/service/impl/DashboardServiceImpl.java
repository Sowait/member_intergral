package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.restaurant.entity.Member;
import com.restaurant.entity.Order;
import com.restaurant.entity.PointRecord;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.mapper.OrderMapper;
import com.restaurant.mapper.PointRecordMapper;
import com.restaurant.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final OrderMapper orderMapper;
    private final PointRecordMapper pointRecordMapper;

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总会员数
        QueryWrapper<Member> memberQuery = new QueryWrapper<>();
        memberQuery.eq("deleted", 0);
        Long totalMembers = memberMapper.selectCount(memberQuery);
        statistics.put("totalMembers", totalMembers != null ? totalMembers : 0);
        
        // 总积分（所有会员的可用积分总和）
        QueryWrapper<Member> pointsQuery = new QueryWrapper<>();
        pointsQuery.select("IFNULL(SUM(available_points), 0) as totalPoints");
        pointsQuery.eq("deleted", 0);
        Map<String, Object> pointsResult = memberMapper.selectMaps(pointsQuery).get(0);
        statistics.put("totalPoints", pointsResult.get("totalPoints"));
        
        // 总订单数
        QueryWrapper<Order> orderQuery = new QueryWrapper<>();
        Long totalOrders = orderMapper.selectCount(orderQuery);
        statistics.put("totalOrders", totalOrders != null ? totalOrders : 0);
        
        // 总兑换数（积分消费记录数）
        QueryWrapper<PointRecord> exchangeQuery = new QueryWrapper<>();
        exchangeQuery.eq("type", "CONSUME");
        Long totalExchanges = pointRecordMapper.selectCount(exchangeQuery);
        statistics.put("totalExchanges", totalExchanges != null ? totalExchanges : 0);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getLevelDistribution() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询各等级会员数量
        QueryWrapper<Member> query = new QueryWrapper<>();
        query.select("level, COUNT(*) as count");
        query.eq("deleted", 0);
        query.groupBy("level");
        
        List<Map<String, Object>> levelData = memberMapper.selectMaps(query);
        
        // 初始化各等级数据
        Map<String, Integer> levelCounts = new HashMap<>();
        levelCounts.put("BRONZE", 0);
        levelCounts.put("SILVER", 0);
        levelCounts.put("GOLD", 0);
        levelCounts.put("PLATINUM", 0);
        
        // 填充实际数据
        int totalCount = 0;
        for (Map<String, Object> item : levelData) {
            String level = (String) item.get("level");
            Long count = (Long) item.get("count");
            if (level != null && count != null) {
                levelCounts.put(level, count.intValue());
                totalCount += count.intValue();
            }
        }
        
        // 计算百分比
        Map<String, Object> distribution = new HashMap<>();
        for (Map.Entry<String, Integer> entry : levelCounts.entrySet()) {
            String level = entry.getKey();
            Integer count = entry.getValue();
            int percentage = totalCount > 0 ? (count * 100 / totalCount) : 0;
            
            Map<String, Object> levelInfo = new HashMap<>();
            levelInfo.put("count", count);
            levelInfo.put("percentage", percentage);
            
            distribution.put(level, levelInfo);
        }
        
        result.put("distribution", distribution);
        result.put("total", totalCount);
        
        return result;
    }
}