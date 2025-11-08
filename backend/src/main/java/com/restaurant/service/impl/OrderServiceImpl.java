package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.entity.Order;
import com.restaurant.mapper.OrderMapper;
import com.restaurant.service.MemberService;
import com.restaurant.service.OrderService;
import com.restaurant.service.PointRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    private final MemberService memberService;
    private final PointRecordService pointRecordService;
    
    @Override
    @Transactional
    public Long createOrder(Long memberId, Long storeId, Double amount, String description) {
        // 计算积分（1元=1积分）
        Integer pointsEarned = amount.intValue();
        
        // 生成订单号
        String orderNo = generateOrderNo();
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setStoreId(storeId);
        order.setAmount(BigDecimal.valueOf(amount));
        order.setPointsEarned(pointsEarned);
        order.setDescription(description);
        order.setOrderTime(LocalDateTime.now());
        
        save(order);
        
        // 更新会员积分
        memberService.updatePoints(memberId, pointsEarned);
        
        // 添加积分记录
        pointRecordService.addPointRecord(
            memberId, 
            pointsEarned, 
            "EARN", 
            order.getId(), 
            "消费获得积分"
        );
        
        return order.getId();
    }
    
    @Override
    public List<Order> findByMemberId(Long memberId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMemberId, memberId)
               .orderByDesc(Order::getOrderTime);
        return list(wrapper);
    }
    
    @Override
    public List<Order> findByStoreId(Long storeId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStoreId, storeId)
               .orderByDesc(Order::getOrderTime);
        return list(wrapper);
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "ORD" + timestamp + random;
    }
}