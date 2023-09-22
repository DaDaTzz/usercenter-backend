package com.da.usercenter.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍更新请求封装类
 */
@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = -8461068150196406472L;
    /**
     * id
     */
    private Long id;


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
     * 0-公开 1-私有 2-加密
     */
    private Integer states;


}
