package com.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restaurant.entity.ExchangeRecord;

import java.util.List;

public interface ExchangeRecordService extends IService<ExchangeRecord> {
    
    /**
     * 创建兑换记录
     * @param memberId 会员ID
     * @param productId 商品ID
     * @param pointsUsed 使用积分
     * @param receiveInfo 领取信息
     * @return 记录ID
     */
    Long createExchangeRecord(Long memberId, Long productId, Integer pointsUsed, String receiveInfo);
    
    /**
     * 根据会员ID查询兑换记录
     * @param memberId 会员ID
     * @return 兑换记录列表
     */
    List<ExchangeRecord> findByMemberId(Long memberId);
    
    /**
     * 根据商品ID查询兑换记录
     * @param productId 商品ID
     * @return 兑换记录列表
     */
    List<ExchangeRecord> findByProductId(Long productId);
}