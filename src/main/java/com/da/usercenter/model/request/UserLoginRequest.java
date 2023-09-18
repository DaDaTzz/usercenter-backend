package com.da.usercenter.model.request;

import lombok.Data;
import java.io.Serializable;

/**
 * @author Da
 * @date 2023/6/10 19:30
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 2869639194267018697L;

    /**
     * 登录账户
     */
    private String loginAccount;

    /**
     * 登录密码
     */
    private String loginPassword;
}
