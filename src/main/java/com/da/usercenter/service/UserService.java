package com.da.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.model.entity.Team;
import com.da.usercenter.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * (User)表服务接口
 *
 * @author Da
 * @since 2023-06-10 13:40:20
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    Long userRegister(String loginAccount, String loginPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String loginAccount, String loginPassword, HttpServletRequest request);

    /**
     * 查询用户
     * @param nickName 昵称
     * @param request
     * @return 用户信息
     */
    List<User> searchUser(String nickName, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param user
     * @param request
     * @return 成功 or 失败
     */
    boolean deleteUser(User user, HttpServletRequest request);

    /**
     * 管理员验证
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User loginUser);

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    User getSafeUser(User user);

    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 注销
     * @param request
     * @return
     */
    Integer userLogOut(HttpServletRequest request);


    /**
     * 根据标签搜索用户
     */
    List<User> searchUsersByTags(List<String> tags);

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    Boolean updateUser(User user, HttpServletRequest request);

    /**
     * 推荐用户
     * @param request
     * @return
     */
    Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);

    /**
     * 创建队伍
     * @param team
     * @return
     */

}

