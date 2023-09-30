package com.da.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除好友请求封装类
 */
@Data
public class DeleteFriendRequest implements Serializable {

    private static final long serialVersionUID = 1367717133889992130L;

    /**
     * 用户id
     */
    private Long id;
}
