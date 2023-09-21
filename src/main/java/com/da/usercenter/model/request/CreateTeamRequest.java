package com.da.usercenter.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CreateTeamRequest implements Serializable {

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
     * 密码
     */
    private String password;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer states;



    private static final long serialVersionUID = -887357874417680596L;
}
