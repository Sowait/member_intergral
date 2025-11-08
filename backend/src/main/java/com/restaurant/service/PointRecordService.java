package com.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restaurant.entity.PointRecord;

import java.util.List;

public interface PointRecordService extends IService<PointRecord> {
    
    /**
     * 添加积分记录
     * @param memberId 会员ID
     * @param pointsChange 积分变化
     * @param type 类型
     * @param orderId 订单ID
     * @param description 描述
     * @return 记录ID
     */
    Long addPointRecord(Long memberId, Integer pointsChange, String type, Long orderId, String description);
    
    /**
     * 根据会员ID查询积分记录
     * @param memberId 会员ID
     * @return 积分记录列表
     */
    List<PointRecord> findByMemberId(Long memberId);
}