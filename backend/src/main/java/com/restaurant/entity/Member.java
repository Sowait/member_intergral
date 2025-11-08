package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("members")
public class Member {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("member_code")
    private String memberCode;
    
    @TableField("level")
    private String level;
    
    @TableField("total_points")
    private Integer totalPoints;
    
    @TableField("available_points")
    private Integer availablePoints;
    
    @TableField(value = "register_time", fill = FieldFill.INSERT)
    private LocalDateTime registerTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    // 用于存储关联的用户信息，不映射到数据库
    @TableField(exist = false)
    private User user;
}