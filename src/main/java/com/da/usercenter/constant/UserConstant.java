package com.da.usercenter.constant;

/**
 * @author Da
 * @date 2023/6/10 20:39
 */
public interface UserConstant {
    /**
     * 用户登录状态
     */
    String USER_LOGIN_STATE = "userLoginState";

    // ################用户权限#############
    /**
     * 默认权限
     */
    Integer DEFAULT_USER = 0;

    /**
     * 管理员权限
     */
    Integer ADMIN_USER = 1;

    // ##################账号状态###############
    /**
     * 账号正常
     */
    Integer USER_ALLOW = 0;

    /**
     * 账号封禁
     */
    Integer USER_DISABLE = 1;

}
