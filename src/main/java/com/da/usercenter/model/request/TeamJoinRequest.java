package com.da.usercenter.model.request;

import lombok.Data;

/**
 * 加入队伍请求封装类
 */
@Data
public class TeamJoinRequest {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String password;
}
