package com.da.usercenter.service.impl;

import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @author Da
 * @date 2023/6/10 17:21
 */
@SpringBootTest
class UserServiceImplTest {
    @Resource
    private UserService userService;
    @Resource
    private HttpServletRequest request;

//    void userRegister() {
//        // 非空
//        String loginAccount = " ";
//        String loginPassword = "12345678";
//        String checkPassword = "12345678";
//        Long result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        // 账户长度不小于6位
//        loginAccount = "Daaaa";
//        result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        // 密码不小于8位
//        loginAccount = "Daaaaa";
//        loginPassword = "1234567";
//        checkPassword = "1234567";
//        result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        // 账户不能包含特殊字符
//        loginAccount = "Da aa";
//        loginPassword = "12345678";
//        checkPassword = "12345678";
//        result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        // 密码和校验密码相同
//        loginAccount = "Daaaaa";
//        checkPassword = "123456789";
//        result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        // 账户不能重复
//        loginAccount = "Daaaaa";
//        loginPassword = "12345678";
//        checkPassword = "12345678";
//        result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        // 插入数据
//        loginAccount = "dadadadadadadadada";
//        loginPassword = "12345678";
//        checkPassword = "12345678";
//        result = userService.userRegister(loginAccount, loginPassword, checkPassword);
//        Assertions.assertTrue(result > 0);
//    }



}
