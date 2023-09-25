package com.da.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类（脱敏）
 *
 * @author 达
 */
@Data
public class TeamUserVO implements Serializable {
    private static final long serialVersionUID = 6339921095990849279L;
    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer states;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 创建人信息
     */
    private UserVO createUser;

    /**
     * 加入队伍用户信息
     */
    private List<UserVO> joinUsers;

    /**
     * 是否已加入
     */
    private boolean hasJoin = false;

}
