package com.da.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍请求封装类
 */
@Data
public class TeamExitRequest implements Serializable {
    private static final long serialVersionUID = 4753284461028427753L;
    /**
     * 队伍id
     */
    private Long teamId;
}
