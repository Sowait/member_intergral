package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
public class Order {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("order_no")
    private String orderNo;
    
    @TableField("member_id")
    private Long memberId;
    
    @TableField("store_id")
    private Long storeId;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("points_earned")
    private Integer pointsEarned;
    
    @TableField(value = "order_time", fill = FieldFill.INSERT)
    private LocalDateTime orderTime;
    
    @TableField("description")
    private String description;
    
    // 用于存储关联的会员信息，不映射到数据库
    @TableField(exist = false)
    private Member member;
    
    // 用于存储关联的门店信息，不映射到数据库
    @TableField(exist = false)
    private Store store;
}