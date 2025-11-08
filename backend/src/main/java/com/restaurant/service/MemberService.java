package com.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restaurant.entity.Member;

public interface MemberService extends IService<Member> {
    
    /**
     * 根据用户ID创建会员
     * @param userId 用户ID
     * @return 会员信息
     */
    Member createMember(Long userId);
    
    /**
     * 根据用户ID查询会员
     * @param userId 用户ID
     * @return 会员信息
     */
    Member findByUserId(Long userId);
    
    /**
     * 更新会员积分
     * @param memberId 会员ID
     * @param pointsChange 积分变化
     * @return 更新结果
     */
    boolean updatePoints(Long memberId, Integer pointsChange);
}
