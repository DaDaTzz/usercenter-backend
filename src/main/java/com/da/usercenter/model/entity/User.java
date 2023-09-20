package com.da.usercenter.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (User)表实体类
 *
 * @author Da
 * @since 2023-06-10 13:24:18
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User implements Serializable {
    /**
     * 主键自增
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 登录密码
     */
    private String loginPassword;

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
     * 逻辑删除
     * 0-未删除
     * 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;



}

