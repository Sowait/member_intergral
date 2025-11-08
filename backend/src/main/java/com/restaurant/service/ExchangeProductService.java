package com.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restaurant.entity.ExchangeProduct;

import java.util.List;

public interface ExchangeProductService extends IService<ExchangeProduct> {
    
    /**
     * 获取上架的商品列表
     * @return 商品列表
     */
    List<ExchangeProduct> getActiveProducts();
    
    /**
     * 兑换商品
     * @param memberId 会员ID
     * @param productId 商品ID
     * @return 兑换结果
     */
    boolean exchangeProduct(Long memberId, Long productId);
}