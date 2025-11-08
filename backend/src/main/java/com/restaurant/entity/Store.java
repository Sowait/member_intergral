package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("stores")
public class Store {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("store_name")
    private String storeName;
    
    @TableField("address")
    private String address;
    
    @TableField("phone")
    private String phone;
    
    @TableField("manager_id")
    private Long managerId;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}