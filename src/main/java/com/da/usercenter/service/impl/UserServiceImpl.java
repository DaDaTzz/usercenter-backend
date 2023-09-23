package com.da.usercenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param checkPassword 校验密码
     * @return 创建成功的用户 id
     */
    @Override
    public Long userRegister(String loginAccount, String loginPassword, String checkPassword) {
        // 非空校验
        if(StrUtil.isAllBlank(loginAccount, loginPassword, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于6位
        if(loginAccount.length() < 6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度小于6位");
        }
        // 密码不小于8位
        if(loginPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8位");
        }
        // 账户不能包含特殊字符
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        if(p.matcher(loginAccount).find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码和校验密码相同
        if(!loginPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不同");
        }
        // 账户不能重复
        Integer count = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).count();
        if(count > 0){
            throw new BusinessException(PARAMS_ERROR,"账户名已存在");
        }
        // 密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + loginPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setLoginAccount(loginAccount);
        user.setLoginPassword(newPassword);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "注册失败，未知原因");
        }
        return user.getId();
    }

    /**
     * 登录
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param request 客户端请求对象
     * @return 登录用户信息
     */
    @Override
    public User userLogin(String loginAccount, String loginPassword, HttpServletRequest request) {
        // 非空校验
        if(StrUtil.isAllBlank(loginAccount, loginPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于6位
        if(loginAccount.length() < 6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号小于6位");
        }
        // 密码不小于8位
        if(loginPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");
        }
        // 账户不能包含特殊字符
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        if(p.matcher(loginAccount).find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 密码加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + loginPassword).getBytes());
        // 校验密码是否输入正确
        User user = this.lambdaQuery().eq(User::getLoginAccount, loginAccount).eq(User::getLoginPassword, newPassword).one();
        if(user == null){
            log.info("loginAccount not matcher loginPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号和密码不匹配");
        }
        // 用户信息脱敏
        User safeUser = getSafeUser(user);
        // 记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        return safeUser;
    }

    /**
     * 通过昵称查询用户信息
     * @param nickName 昵称
     * @param request 客户端请求对象
     * @return userList
     */
    @Override
    public List<User> searchUser(String nickName, HttpServletRequest request) {
        // 权限校验
        if(!isAdmin(request)){
            throw new BusinessException(NO_AUTH);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(nickName)){
            queryWrapper.like(User::getNickname, nickName);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * 删除用户
     * @param user 用户信息
     * @param request 客户端请求对象
     * @return boolean
     */
    @Override
    public boolean deleteUser(User user, HttpServletRequest request) {
        // 权限校验
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(user.getId() < 0){
            throw  new BusinessException(ErrorCode.DATABASE_ERROR, "删除失败");
        }
        return removeById(user.getId());
    }

    /**
     * 判断是否为管理员
     * @param request 客户端请求对象
     * @return boolean
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) attribute;
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"未登录");
        }
        // 权限校验
        return user.getType().equals(ADMIN_USER);
    }

    /**
     * 判断是否为管理员
     * @param loginUser 已登录用户信息
     * @return boolean
     */
    @Override
    public boolean isAdmin(User loginUser) {
        if(loginUser == null ){
            throw new BusinessException(NOT_LOGIN);
        }
        return loginUser.getType().equals(ADMIN_USER);
    }


    @Override
    public User getSafeUser(User user) {
        if(user == null){
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
     * @param request 客户端请求对象
     * @return 当前用户信息
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        // 查询数据库，获取最新用户信息
        User user = this.getById(currentUser.getId());
        // 判断账号状态
        if(USER_DISABLE.equals(user.getStates())){
            throw new BusinessException(USER_STATE_ERROR, "账号已被封禁");
        }
        return this.getSafeUser(user);
    }

    /**
     * 注销
     * @param request 客户端请求对象
     * @return 1-注销成功
     */
    @Override
    public Integer userLogOut(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(NOT_LOGIN, "未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 通过标签搜索用户信息
     * @param tagNameList 标签 list
     * @return userList
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
       // ArrayList<User> users = new ArrayList<>();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper.like(User::getTags,tagName);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());

    }

    /**
     * 获取登录用户信息
     * @param request 客户端请求对象
     * @return 登录用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(NOT_LOGIN);
        }
        return (User)userObj;
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
        User loginUser = this.getLoginUser(request);
        // 校验是否为管理员或自己
        if(isAdmin(loginUser)){
            return this.updateById(user);
        }
        if(loginUser.getId() != user.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限");
        }
        // 3.触发更新
        return this.updateById(user);
    }

    /**
     * 推荐用户
     * @param request 客户端请求对象
     * @return 推荐用户分页对象
     */
    @Override
    public Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 如果有缓存，直接从缓存中读取用户信息
        User loginUser = this.getLoginUser(request);
        long userId = loginUser.getId();
        String redisKey = "user:recommend:" + userId;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if(userPage != null){
            return userPage;
        }
        // 无缓存，查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 将读到的数据存入缓存中
        try {
            valueOperations.set(redisKey, userPage, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return userPage;
    }



}

