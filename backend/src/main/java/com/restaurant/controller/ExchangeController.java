package com.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.entity.ExchangeProduct;
import com.restaurant.entity.ExchangeRecord;
import com.restaurant.entity.Member;
import com.restaurant.service.ExchangeProductService;
import com.restaurant.service.ExchangeRecordService;
import com.restaurant.service.MemberService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {
    
    private final ExchangeProductService exchangeProductService;
    private final ExchangeRecordService exchangeRecordService;
    private final MemberService memberService;
    private final UserService userService;
    
    @GetMapping("/products")
    public Result<List<ExchangeProduct>> getProducts() {
        List<ExchangeProduct> products = exchangeProductService.getActiveProducts();
        return Result.success(products);
    }
    @PostMapping("/exchange")
    public Result<String> exchangeProduct(@RequestBody ExchangeRequest request) {
        try {
            // 获取当前用户
            String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
            
            // 获取会员信息
            com.restaurant.entity.User user = userService.findByUsername(username);
            Member member = memberService.findByUserId(user.getId());
            
            // 执行兑换
            boolean success = exchangeProductService.exchangeProduct(member.getId(), request.getProductId());
            if (success) {
                return Result.success("兑换成功");
            } else {
                return Result.error("兑换失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    @GetMapping("/records")
    public Result<List<ExchangeRecord>> getMyExchangeRecords() {
        // 获取当前用户
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
        
        // 获取会员信息
        com.restaurant.entity.User user = userService.findByUsername(username);
        Member member = memberService.findByUserId(user.getId());
        
        List<ExchangeRecord> records = exchangeRecordService.findByMemberId(member.getId());
        return Result.success(records);
    }
    @GetMapping("/records/list")
    public Result<Page<ExchangeRecord>> getAllExchangeRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String keyword) {
        
        Page<ExchangeRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (productId != null) {
            wrapper.eq(ExchangeRecord::getProductId, productId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ExchangeRecord::getReceiveInfo, keyword);
        }
        
        wrapper.orderByDesc(ExchangeRecord::getExchangeTime);
        
        Page<ExchangeRecord> result = exchangeRecordService.page(page, wrapper);
        return Result.success(result);
    }
    @GetMapping("/products/manage")
    public Result<Page<ExchangeProduct>> getProductsForManage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        Page<ExchangeProduct> page = new Page<>(current, size);
        LambdaQueryWrapper<ExchangeProduct> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ExchangeProduct::getProductName, keyword)
                   .or()
                   .like(ExchangeProduct::getDescription, keyword);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ExchangeProduct::getStatus, status);
        }
        
        wrapper.orderByDesc(ExchangeProduct::getCreateTime);
        
        Page<ExchangeProduct> result = exchangeProductService.page(page, wrapper);
        return Result.success(result);
    }
    @PostMapping("/products")
    public Result<String> createProduct(@RequestBody ExchangeProduct product) {
        try {
            // 创建商品时清空ID，让数据库自动生成
            product.setId(null);
            product.setStatus("ACTIVE");
            boolean success = exchangeProductService.save(product);
            if (success) {
                return Result.success("商品创建成功");
            } else {
                return Result.error("商品创建失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    @PutMapping("/products/{id}")
    public Result<String> updateProduct(@PathVariable Long id, @RequestBody ExchangeProduct product) {
        try {
            product.setId(id);
            boolean success = exchangeProductService.updateById(product);
            if (success) {
                return Result.success("商品更新成功");
            } else {
                return Result.error("商品更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    @DeleteMapping("/products/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        try {
            boolean success = exchangeProductService.removeById(id);
            if (success) {
                return Result.success("商品删除成功");
            } else {
                return Result.error("商品删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 兑换请求DTO
    public static class ExchangeRequest {
        private Long productId;
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
    }
}