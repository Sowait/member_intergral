package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exchange_records")
public class ExchangeRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("member_id")
    private Long memberId;
    
    @TableField("product_id")
    private Long productId;
    
    @TableField("points_used")
    private Integer pointsUsed;
    
    @TableField(value = "exchange_time", fill = FieldFill.INSERT)
    private LocalDateTime exchangeTime;
    
    @TableField("receive_info")
    private String receiveInfo;
}