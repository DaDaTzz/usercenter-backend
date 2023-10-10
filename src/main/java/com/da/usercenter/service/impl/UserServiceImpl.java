package com.da.usercenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.request.DeleteRequest;
import com.da.usercenter.model.request.QueryUserByConditionRequest;
import com.da.usercenter.model.vo.UserVO;
import com.da.usercenter.service.UserService;
import com.da.usercenter.utils.AlgorithmUtil;
import com.da.usercenter.utils.TokenUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.da.usercenter.common.ErrorCode.*;
import static com.da.usercenter.constant.UserConstant.*;

/**
 * (User)表服务实现类
 *
 * @author Da
 * @since 2023-06-10 13:40:21
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "Da";


    /**
     * 注册
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param checkPassword 校验密码
     * @return 创建成功的用户 id
     */
    @Override
    public Long userRegister(String loginAccount, String loginPassword, String checkPassword, String nickname) {
        // 非空校验
        if (StrUtil.isBlank(loginAccount) || StrUtil.isBlank(loginPassword) || StrUtil.isBlank(checkPassword) || StrUtil.isBlank(nickname)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于6位
        if (loginAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度小于6位");
        }
        // 密码不小于8位
        if (loginPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8位");
        }
        // 账户不能包含特殊字符
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        if (p.matcher(loginAccount).find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码和校验密码相同
        if (!loginPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不同");
        }
        // 昵称不为空
        if (StringUtils.isBlank(nickname)) {
            throw new BusinessException(NULL_ERROR, "昵称能为空");
        }
        // 账户不能重复
        Integer count = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).count();
        if (count > 0) {
            throw new BusinessException(PARAMS_ERROR, "账户名已存在");
        }
        // 密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + loginPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setLoginAccount(loginAccount);
        user.setLoginPassword(newPassword);
        user.setNickname(nickname);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "注册失败，未知原因");
        }
        return user.getId();
    }

    /**
     * 登录
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param request       客户端请求对象
     * @return 登录用户信息
     */
    @Override
    public User userLogin(String loginAccount, String loginPassword, HttpServletRequest request) {
        // 非空校验
        if (StrUtil.isAllBlank(loginAccount, loginPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于6位
        if (loginAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号小于6位");
        }
        // 密码不小于8位
        if (loginPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");
        }
        // 账户不能包含特殊字符
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        if (p.matcher(loginAccount).find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + loginPassword).getBytes());
        // 校验密码是否输入正确
        User user = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).eq(User::getLoginPassword, newPassword).one();
        if (user == null) {
            log.info("loginAccount not matcher loginPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号和密码不匹配");
        }
        User safeUser = this.getSafeUser(user);
        // 将登录信息存入redis 单点登录
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("user:login:" + user.getId(), safeUser, 30, TimeUnit.MINUTES);
        return safeUser;
    }

    /**
     * 通过昵称查询用户信息
     *
     * @param nickName 昵称
     * @param request  客户端请求对象
     * @return userList
     */
    @Override
    public List<User> searchUser(String nickName, HttpServletRequest request) {
        // 权限校验
        if (!isAdmin(request)) {
            throw new BusinessException(NO_AUTH);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(nickName)) {
            queryWrapper.like(User::getNickname, nickName);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest id
     * @param request       客户端请求对象
     * @return boolean
     */
    @Override
    public boolean deleteUser(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 权限校验
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (deleteRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除失败");
        }
        boolean res = removeById(deleteRequest.getId());
        redisTemplate.delete("user:login:" + deleteRequest.getId());
        // 更新推荐用户列表 防止脏数据
        Set<String> keys = redisTemplate.keys("user:recommend:" + "*");
        redisTemplate.delete(keys);
        return res;
    }

    /**
     * 判断是否为管理员
     *
     * @param request 客户端请求对象
     * @return boolean
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = this.getCurrentUser(request);
        if (user == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 权限校验
        return ADMIN_USER.equals(user.getType());
    }

    /**
     * 判断是否为管理员
     *
     * @param loginUser 已登录用户信息
     * @return boolean
     */
    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        return ADMIN_USER.equals(loginUser.getType());
    }


    @Override
    public User getSafeUser(User user) {
        if (user == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setNickname(user.getNickname());
        safeUser.setLoginAccount(user.getLoginAccount());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setSex(user.getSex());
        safeUser.setStates(user.getStates());
        safeUser.setProfilePhoto(user.getProfilePhoto());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setType(user.getType());
        safeUser.setTags(user.getTags());
        safeUser.setProfile(user.getProfile());
        return safeUser;
    }

    /**
     * 获取当前用户信息
     *
     * @param request 客户端请求对象
     * @return 当前用户信息
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(NOT_LOGIN, "未登录");
        }
        String userId = TokenUtils.getAccount(token);
        // 从缓存中获取用户信息
        User user = (User) redisTemplate.opsForValue().get("user:login:" + userId);
        if (user == null) {
            throw new BusinessException(LOGIN_EXPIRE);
        }
        if (user != null && USER_DISABLE.equals(user.getStates())) {
            throw new BusinessException(USER_STATE_ERROR, "账号已被封禁");
        }
        return this.getSafeUser(user);
    }

    /**
     * 注销
     *
     * @param request 客户端请求对象
     * @return 1-注销成功
     */
    @Override
    public Boolean userLogOut(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(NULL_ERROR);
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(NOT_LOGIN);
        }
        if (!TokenUtils.verify(token)) {
            throw new BusinessException(LOGIN_EXPIRE);
        }
        String userId = TokenUtils.getAccount(token);
        // 移除 redis 中的用户信息
        Boolean res = redisTemplate.delete("user:login:" + userId);
        return res;
    }


    /**
     * 更新用户
     *
     * @param user 更新用户信息
     * @return boolean
     */
    @Override
    public Boolean updateUser(User user, HttpServletRequest request) {
        // 1.校验参数是否为空
        if (user == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 2.权限校验
        User loginUser = this.getCurrentUser(request);
        // 校验是否为管理员或自己
        if (isAdmin(loginUser) || loginUser.getId() == user.getId()) {
            boolean res = this.updateById(user);
            if (res) {
                // 昵称不能为空
                if (StringUtils.isBlank(user.getNickname())) {
                    user.setNickname("无名氏");
                    User newUserInfo = this.getById(user.getId());
                    User safeUser = this.getSafeUser(newUserInfo);
                    redisTemplate.opsForValue().set("user:login:" + user.getId(), safeUser, 30, TimeUnit.MINUTES);
                }
            }
            // 执行更新操作后更新 redis 中的缓存数据， 保证数据的一致性
            User newUserInfo = this.getById(user.getId());
            User safeUser = this.getSafeUser(newUserInfo);
            redisTemplate.opsForValue().set("user:login:" + user.getId(), safeUser, 30, TimeUnit.MINUTES);
            // 更新推荐用户列表 防止数据不统一
            Set<String> keys = redisTemplate.keys("user:recommend:" + "*");
            redisTemplate.delete(keys);
            return res;
        }
        if (loginUser.getId() != user.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有权限");
        }
        return false;
    }

    @Override
    public User queryUserById(long id, HttpServletRequest request) {
        User currentUser = this.getCurrentUser(request);
        if (currentUser == null || !ADMIN_USER.equals(currentUser.getType())) {
            throw new BusinessException(NO_AUTH, "没有权限");
        }
        if(id <= 0){
            throw new BusinessException(PARAMS_ERROR);
        }
        User user = this.getById(id);
        return user;
    }


}

