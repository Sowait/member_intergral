package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.entity.ExchangeProduct;
import com.restaurant.entity.ExchangeRecord;
import com.restaurant.entity.Member;
import com.restaurant.mapper.ExchangeProductMapper;
import com.restaurant.service.ExchangeProductService;
import com.restaurant.service.ExchangeRecordService;
import com.restaurant.service.MemberService;
import com.restaurant.service.PointRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeProductServiceImpl extends ServiceImpl<ExchangeProductMapper, ExchangeProduct> implements ExchangeProductService {
    
    private final MemberService memberService;
    private final ExchangeRecordService exchangeRecordService;
    private final PointRecordService pointRecordService;
    
    @Override
    public List<ExchangeProduct> getActiveProducts() {
        LambdaQueryWrapper<ExchangeProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeProduct::getStatus, "ACTIVE")
               .eq(ExchangeProduct::getDeleted, 0)
               .orderByAsc(ExchangeProduct::getPointsRequired);
        return list(wrapper);
    }
    
    @Override
    @Transactional
    public boolean exchangeProduct(Long memberId, Long productId) {
        // 查询商品信息
        ExchangeProduct product = getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        if (!"ACTIVE".equals(product.getStatus())) {
            throw new RuntimeException("商品已下架");
        }
        
        if (product.getStockQuantity() <= 0) {
            throw new RuntimeException("商品库存不足");
        }
        
        // 查询会员信息
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        if (member.getAvailablePoints() < product.getPointsRequired()) {
            throw new RuntimeException("积分不足");
        }
        
        // 扣减会员积分
        memberService.updatePoints(memberId, -product.getPointsRequired());
        
        // 添加积分消费记录
        pointRecordService.addPointRecord(
            memberId,
            -product.getPointsRequired(),
            "CONSUME",
            null,
            "兑换商品：" + product.getProductName()
        );
        
        // 减少商品库存
        product.setStockQuantity(product.getStockQuantity() - 1);
        updateById(product);
        
        // 创建兑换记录
        exchangeRecordService.createExchangeRecord(
            memberId,
            productId,
            product.getPointsRequired(),
            "兑换成功，商品已发放"
        );
        
        return true;
    }
}