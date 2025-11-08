package com.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("point_records")
public class PointRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("member_id")
    private Long memberId;
    
    @TableField("points_change")
    private Integer pointsChange;
    
    @TableField("type")
    private String type;
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("description")
    private String description;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
