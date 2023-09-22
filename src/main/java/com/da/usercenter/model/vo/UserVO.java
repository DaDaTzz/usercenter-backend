package com.da.usercenter.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户包装类
 */
@Data
public class UserVO {
    /**
     * 主键自增
     */
    private long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     * 0-女
     * 1-男
     */
    private Integer sex;

    /**
     * 登录账号
     */
    private String loginAccount;

    /**
     * 个人简介
     */
    private String profile;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;


    /**
     * 标签列表
     */
    private String tags;

    /**
     * 账号状态
     * 0-正常使用
     * 1-封号
     */
    private Integer states;

    /**
     * 头像
     */
    private String profilePhoto;

    /**
     * 用户权限
     * 0-默认权限
     * 1-管理员
     */
    private Integer type;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
