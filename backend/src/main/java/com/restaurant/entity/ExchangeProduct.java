package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exchange_products")
public class ExchangeProduct {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("product_name")
    private String productName;
    
    @TableField("description")
    private String description;
    
    @TableField("points_required")
    private Integer pointsRequired;
    
    @TableField("stock_quantity")
    private Integer stockQuantity;
    
    @TableField("image_url")
    private String imageUrl;
    
    @TableField("status")
    private String status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}