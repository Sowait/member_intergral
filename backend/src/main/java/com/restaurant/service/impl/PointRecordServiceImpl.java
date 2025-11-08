package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.entity.PointRecord;
import com.restaurant.mapper.PointRecordMapper;
import com.restaurant.service.PointRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointRecordServiceImpl extends ServiceImpl<PointRecordMapper, PointRecord> implements PointRecordService {
    
    @Override
    public Long addPointRecord(Long memberId, Integer pointsChange, String type, Long orderId, String description) {
        PointRecord record = new PointRecord();
        record.setMemberId(memberId);
        record.setPointsChange(pointsChange);
        record.setType(type);
        record.setOrderId(orderId);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        
        save(record);
        return record.getId();
    }
    
    @Override
    public List<PointRecord> findByMemberId(Long memberId) {
        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointRecord::getMemberId, memberId)
               .orderByDesc(PointRecord::getCreateTime);
        return list(wrapper);
    }
}