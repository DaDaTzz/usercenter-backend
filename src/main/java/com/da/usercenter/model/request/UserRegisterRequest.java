package com.da.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Da
 * &#064;date  2023/6/10 19:22
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3114662314076468474L;

    /**
     * 登录账户
     */
    private String loginAccount;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 二次输入密码
     */
    private String checkPassword;
}
