package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.entity.ExchangeRecord;
import com.restaurant.mapper.ExchangeRecordMapper;
import com.restaurant.service.ExchangeRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExchangeRecordServiceImpl extends ServiceImpl<ExchangeRecordMapper, ExchangeRecord> implements ExchangeRecordService {
    
    @Override
    public Long createExchangeRecord(Long memberId, Long productId, Integer pointsUsed, String receiveInfo) {
        ExchangeRecord record = new ExchangeRecord();
        record.setMemberId(memberId);
        record.setProductId(productId);
        record.setPointsUsed(pointsUsed);
        record.setReceiveInfo(receiveInfo);
        record.setExchangeTime(LocalDateTime.now());
        
        save(record);
        return record.getId();
    }
    
    @Override
    public List<ExchangeRecord> findByMemberId(Long memberId) {
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeRecord::getMemberId, memberId)
               .orderByDesc(ExchangeRecord::getExchangeTime);
        return list(wrapper);
    }
    
    @Override
    public List<ExchangeRecord> findByProductId(Long productId) {
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeRecord::getProductId, productId)
               .orderByDesc(ExchangeRecord::getExchangeTime);
        return list(wrapper);
    }
}