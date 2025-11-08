package com.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restaurant.entity.Member;
import com.restaurant.mapper.MemberMapper;
import com.restaurant.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {
    
    @Override
    @Transactional
    public Member createMember(Long userId) {
        // 检查会员是否已存在
        if (findByUserId(userId) != null) {
            throw new RuntimeException("会员已存在");
        }
        
        // 生成会员号
        String memberCode = generateMemberCode();
        
        Member member = new Member();
        member.setUserId(userId);
        member.setMemberCode(memberCode);
        member.setLevel("BRONZE"); // 默认青铜会员
        member.setTotalPoints(0);
        member.setAvailablePoints(0);
        member.setRegisterTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        
        save(member);
        return member;
    }
    
    @Override
    public Member findByUserId(Long userId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        return getOne(wrapper);
    }
    
    @Override
    @Transactional
    public boolean updatePoints(Long memberId, Integer pointsChange) {
        Member member = getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        // 检查积分是否足够（消费时）
        if (pointsChange < 0 && member.getAvailablePoints() + pointsChange < 0) {
            throw new RuntimeException("积分不足");
        }
        
        // 更新积分
        member.setTotalPoints(member.getTotalPoints() + pointsChange);
        member.setAvailablePoints(member.getAvailablePoints() + pointsChange);
        
        // 更新会员等级（可选）
        updateMemberLevel(member);
        
        return updateById(member);
    }
    
    /**
     * 生成会员号
     */
    private String generateMemberCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "M" + timestamp + random;
    }
    
    /**
     * 更新会员等级
     */
    private void updateMemberLevel(Member member) {
        int totalPoints = member.getTotalPoints();
        if (totalPoints >= 5000) {
            member.setLevel("PLATINUM");
        } else if (totalPoints >= 2000) {
            member.setLevel("GOLD");
        } else if (totalPoints >= 500) {
            member.setLevel("SILVER");
        } else {
            member.setLevel("BRONZE");
        }
    }
}