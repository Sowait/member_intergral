package com.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.entity.Order;
import com.restaurant.entity.Store;
import com.restaurant.service.OrderService;
import com.restaurant.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final StoreService storeService;
    private final com.restaurant.service.UserService userService;
    private final com.restaurant.service.MemberService memberService;
    
    @PostMapping
    public Result<String> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            orderService.createOrder(
                request.getMemberId(),
                request.getStoreId(),
                request.getAmount(),
                request.getDescription()
            );
            return Result.success("消费记录创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/member/{memberId}")
    public Result<List<Order>> getOrdersByMember(@PathVariable Long memberId) {
        List<Order> orders = orderService.findByMemberId(memberId);
        return Result.success(orders);
    }
    
    @GetMapping("/my-orders")
    public Result<Map<String, Object>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        
        // 获取当前用户
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
        
        // 获取会员信息
        com.restaurant.entity.User user = userService.findByUsername(username);
        com.restaurant.entity.Member member = memberService.findByUserId(user.getId());
        
        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMemberId, member.getId());
        
        if (storeId != null) {
            wrapper.eq(Order::getStoreId, storeId);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(Order::getOrderTime, startDate + " 00:00:00");
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(Order::getOrderTime, endDate + " 23:59:59");
        }
        
        if (minAmount != null) {
            wrapper.ge(Order::getAmount, minAmount);
        }
        
        if (maxAmount != null) {
            wrapper.le(Order::getAmount, maxAmount);
        }
        
        wrapper.orderByDesc(Order::getOrderTime);
        
        // 分页查询
        Page<Order> page = new Page<>(current, size);
        Page<Order> result = orderService.page(page, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    @GetMapping("/store/{storeId}")
    public Result<List<Order>> getOrdersByStore(@PathVariable Long storeId) {
        List<Order> orders = orderService.findByStoreId(storeId);
        return Result.success(orders);
    }
    
    @GetMapping("/list")
    public Result<Page<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword) {
        
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(Order::getStoreId, storeId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, keyword)
                   .or()
                   .like(Order::getDescription, keyword);
        }
        
        wrapper.orderByDesc(Order::getOrderTime);
        
        Page<Order> result = orderService.page(page, wrapper);
        
        // 为每个订单加载会员和用户信息
        result.getRecords().forEach(order -> {
            // 加载会员信息
            com.restaurant.entity.Member member = memberService.getById(order.getMemberId());
            if (member != null) {
                // 加载用户信息
                com.restaurant.entity.User user = userService.getById(member.getUserId());
                member.setUser(user);
                order.setMember(member);
            }
            
            // 加载门店信息
            com.restaurant.entity.Store store = storeService.getById(order.getStoreId());
            order.setStore(store);
        });
        
        return Result.success(result);
    }
    
    @GetMapping("/stores")
    public Result<List<Store>> getStores() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getDeleted, 0);
        List<Store> stores = storeService.list(wrapper);
        return Result.success(stores);
    }
    
    @GetMapping("/my-statistics")
    public Result<Map<String, Object>> getMyOrderStatistics(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {
        
        // 获取当前用户
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
        
        // 获取会员信息
        com.restaurant.entity.User user = userService.findByUsername(username);
        com.restaurant.entity.Member member = memberService.findByUserId(user.getId());
        
        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMemberId, member.getId());
        
        if (storeId != null) {
            wrapper.eq(Order::getStoreId, storeId);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(Order::getOrderTime, startDate + " 00:00:00");
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(Order::getOrderTime, endDate + " 23:59:59");
        }
        
        if (minAmount != null) {
            wrapper.ge(Order::getAmount, minAmount);
        }
        
        if (maxAmount != null) {
            wrapper.le(Order::getAmount, maxAmount);
        }
        
        // 查询所有符合条件的订单
        List<Order> orders = orderService.list(wrapper);
        
        // 计算统计数据
        int orderCount = orders.size();
        double totalAmount = 0;
        int totalPoints = 0;
        
        for (Order order : orders) {
            totalAmount += order.getAmount().doubleValue();
            totalPoints += order.getPointsEarned();
        }
        
        double averageAmount = orderCount > 0 ? totalAmount / orderCount : 0;
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("orderCount", orderCount);
        statistics.put("totalAmount", totalAmount);
        statistics.put("totalPoints", totalPoints);
        statistics.put("averageAmount", averageAmount);
        
        return Result.success(statistics);
    }
    
    // 创建订单请求DTO
    public static class CreateOrderRequest {
        private Long memberId;
        private Long storeId;
        private Double amount;
        private String description;
        
        // getters and setters
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}