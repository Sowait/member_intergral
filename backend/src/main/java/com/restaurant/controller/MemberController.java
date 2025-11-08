package com.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.common.Result;
import com.restaurant.entity.Member;
import com.restaurant.entity.PointRecord;
import com.restaurant.entity.User;
import com.restaurant.service.MemberService;
import com.restaurant.service.PointRecordService;
import com.restaurant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    private final UserService userService;
    private final PointRecordService pointRecordService;
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        // 从SecurityContext获取当前用户信息
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
        
        User user = userService.findByUsername(username);
        Member member = memberService.findByUserId(user.getId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("member", member);
        
        return Result.success(data);
    }
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            // 从SecurityContext获取当前用户信息
            String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
            
            User user = userService.findByUsername(username);
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            
            boolean success = userService.updateById(user);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    @GetMapping("/points")
    public Result<Map<String, Object>> getPointRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        String username = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
        
        User user = userService.findByUsername(username);
        Member member = memberService.findByUserId(user.getId());
        
        // 构建查询条件
        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointRecord::getMemberId, member.getId());
        
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(PointRecord::getType, type);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(PointRecord::getCreateTime, startDate + " 00:00:00");
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(PointRecord::getCreateTime, endDate + " 23:59:59");
        }
        
        wrapper.orderByDesc(PointRecord::getCreateTime);
        
        // 分页查询
        Page<PointRecord> page = new Page<>(current, size);
        Page<PointRecord> result = pointRecordService.page(page, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    @GetMapping("/list")
    public Result<Page<Member>> getMemberList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Page<Member> page = new Page<>(current, size);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Member::getMemberCode, keyword)
                    .or()
                    .eq(Member::getUserId, 
                        userService.lambdaQuery()
                            .like(User::getUsername, keyword)
                            .one() != null ? 
                            userService.lambdaQuery()
                                .like(User::getUsername, keyword)
                                .one().getId() : null));
        }
        
        if (level != null && !level.trim().isEmpty()) {
            wrapper.eq(Member::getLevel, level);
        }
        
        if (startDate != null && !startDate.trim().isEmpty()) {
            wrapper.ge(Member::getRegisterTime, startDate + " 00:00:00");
        }
        
        if (endDate != null && !endDate.trim().isEmpty()) {
            wrapper.le(Member::getRegisterTime, endDate + " 23:59:59");
        }
        
        wrapper.orderByDesc(Member::getUpdateTime);
        
        Page<Member> result = memberService.page(page, wrapper);
        
        // 为每个会员加载用户信息
        result.getRecords().forEach(member -> {
            User user = userService.getById(member.getUserId());
            // 使用反射或者添加一个transient字段来存储用户信息
            // 这里我们使用一个简单的方法：在Member实体中添加一个transient字段
            member.setUser(user);
        });
        
        return Result.success(result);
    }
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getMemberDetail(@PathVariable Long id) {
        Member member = memberService.getById(id);
        if (member == null) {
            return Result.error("会员不存在");
        }
        
        User user = userService.getById(member.getUserId());
        List<PointRecord> records = pointRecordService.findByMemberId(member.getId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("member", member);
        data.put("user", user);
        data.put("pointRecords", records);
        
        return Result.success(data);
    }
    @PutMapping("/{id}")
    public Result<String> updateMember(@PathVariable Long id, @RequestBody Member member) {
        member.setId(id);
        boolean success = memberService.updateById(member);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }
    
    // 更新个人资料请求DTO
    public static class UpdateProfileRequest {
        private String phone;
        private String email;
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}