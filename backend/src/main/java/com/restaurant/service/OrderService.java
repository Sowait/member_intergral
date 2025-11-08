package com.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restaurant.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    
    /**
     * 创建消费记录
     * @param memberId 会员ID
     * @param storeId 门店ID
     * @param amount 消费金额
     * @param description 描述
     * @return 订单ID
     */
    Long createOrder(Long memberId, Long storeId, Double amount, String description);
    
    /**
     * 根据会员ID查询消费记录
     * @param memberId 会员ID
     * @return 消费记录列表
     */
    List<Order> findByMemberId(Long memberId);
    
    /**
     * 根据门店ID查询消费记录
     * @param storeId 门店ID
     * @return 消费记录列表
     */
    List<Order> findByStoreId(Long storeId);
}