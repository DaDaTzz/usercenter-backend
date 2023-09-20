package com.da.usercenter.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.request.UserLoginRequest;
import com.da.usercenter.model.request.UserRegisterRequest;
import com.da.usercenter.service.UserService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * (User)表控制层
 *
 * @author Da
 * @since 2023-06-10 13:40:19
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials = "true" )
public class UserController{
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public ResponseResult<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String loginAccount = userRegisterRequest.getLoginAccount();
        String loginPassword = userRegisterRequest.getLoginPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(StrUtil.isAllBlank(loginAccount, loginPassword, checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long result = userService.userRegister(loginAccount, loginPassword, checkPassword);
        return ResponseResult.success(result, "注册成功！");
    }

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
        User result = userService.userLogin(loginAccount, loginPassword, request);
        return ResponseResult.success(result, "登录成功");
    }

    @PostMapping("/logout")
    public ResponseResult<Integer> userLogOut(HttpServletRequest request){
        Integer result = userService.userLogOut(request);
        return ResponseResult.success(result, "注销成功");
    }

    @GetMapping("/search")
    public ResponseResult<List<User>> searchUser(String nickName, HttpServletRequest request){
        List<User> result = userService.searchUser(nickName, request);
        return ResponseResult.success(result);
    }

    @GetMapping("/recommend")
    public ResponseResult<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        Page<User> userPage = userService.recommendUsers(pageSize, pageNum, request);
        return ResponseResult.success(userPage);
    }

    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteUser(@RequestBody User user, HttpServletRequest request){
        boolean result = userService.deleteUser(user, request);
        return ResponseResult.success(result);
    }

    @GetMapping("/current")
    public ResponseResult<User> getCurrentUser(HttpServletRequest request){
        User result = userService.getCurrentUser(request);
        return ResponseResult.success(result);
    }

    @PostMapping("/update")
    public ResponseResult<Boolean> updateUser(@RequestBody User user,HttpServletRequest request){
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean res = userService.updateUser(user, request);
        return ResponseResult.success(res);
    }

    @GetMapping("/search/tags")
    public ResponseResult<List<User>> getUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResponseResult.success(userList);
    }



}

