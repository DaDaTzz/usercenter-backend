package com.da.usercenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "Da";


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
        return userList.stream().map(user -> getSafeUser(user)).collect(Collectors.toList());
    }

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

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) attribute;
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"未登录");
        }
        // 权限校验
        if(user.getType() != ADMIN_USER){
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限");
        }
        return true;
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
        if(user.getStates() == USER_DISABLE){
            throw new BusinessException(USER_STATE_ERROR, "账号已被封禁");
        }
        return this.getSafeUser(user);
    }

    @Override
    public Integer userLogOut(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(NOT_LOGIN, "未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

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
        /*Gson gson = new Gson();
        List<User> userList = this.list();
        for (User user : userList) {
            String tagStr = user.getTags();
            if(StringUtils.isNotBlank(tagStr)){
                Set<String> tempTagNames = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
                for (String s : tagNamelist) {
                    if(tempTagNames.contains(s)){
                        users.add(user);
                        break;
                    }
                }
            }
        }*/
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());

    }



}

