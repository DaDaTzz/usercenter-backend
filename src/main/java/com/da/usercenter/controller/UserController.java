package com.da.usercenter.controller;



import cn.hutool.core.util.StrUtil;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.request.*;
import com.da.usercenter.service.UserService;
import com.da.usercenter.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.da.usercenter.constant.UserConstant.ADMIN_USER;

/**
 * 用户接口
 *
 * @author 达
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:8081"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://8.130.133.165:81"},allowCredentials = "true")
public class UserController{
    @Resource
    private UserService userService;


    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public ResponseResult<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long res = userService.userRegister(userRegisterRequest.getLoginAccount(),userRegisterRequest.getLoginPassword(), userRegisterRequest.getCheckPassword(),userRegisterRequest.getNickname());
        return ResponseResult.success(res, "注册成功！");
    }

    /**
     * 登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseResult<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String loginAccount = userLoginRequest.getLoginAccount();
        String loginPassword = userLoginRequest.getLoginPassword();
        if(StrUtil.isAllBlank(loginAccount, loginPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User safeUser = userService.userLogin(loginAccount, loginPassword, request);
        if(safeUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String token = TokenUtils.getToken(String.valueOf(safeUser.getId()));
        // 将用户信息存入redis
        return ResponseResult.success(safeUser, token);
    }

    /**
     * 注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResponseResult<Boolean> userLogOut(HttpServletRequest request){
        Boolean result = userService.userLogOut(request);
        return ResponseResult.success(result, "注销成功");
    }


    /**
     * 根据条件查询用户信息
     * @param nickName
     * @param request
     * @return
     */
    @GetMapping("/search")
    public ResponseResult<List<User>> searchUser(String nickName, HttpServletRequest request){
        List<User> result = userService.searchUser(nickName, request);
        return ResponseResult.success(result);
    }



    /**
     * 删除用户
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        boolean result = userService.deleteUser(deleteRequest, request);
        return ResponseResult.success(result);
    }

    /**
     * 添加用户（仅限管理员调用）
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> addUser(@RequestBody User user, HttpServletRequest request){

        return ResponseResult.success(userService.save(user));
    }

    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    @GetMapping("/current")
    public ResponseResult<User> getCurrentUser(HttpServletRequest request){
        User result = userService.getCurrentUser(request);
        return ResponseResult.success(result);
    }

    /**
     * 更新用户信息
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> updateUser(@RequestBody User user,HttpServletRequest request){
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean res = userService.updateUser(user, request);
        return ResponseResult.success(res);
    }

    /**
     * 获取所有用户信息（仅限管理员调用）
     * @param request
     * @return
     */
    @GetMapping("/list")
    public ResponseResult<List<User>> getUserList(HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);

        if(!ADMIN_USER.equals(currentUser.getType())){
            throw new BusinessException(ErrorCode.NO_AUTH, "没有权限");
        }

        List<User> userList = userService.list();
        return ResponseResult.success(userList);
    }

    /**
     * 多条件搜索用户（仅限管理员调用）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/getById")
    public ResponseResult<User> queryUserById(long id, HttpServletRequest request){
        User user = userService.queryUserById(id,  request);
        return ResponseResult.success(user);
    }









}

