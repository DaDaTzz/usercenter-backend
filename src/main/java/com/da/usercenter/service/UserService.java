package com.da.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.request.AddFriendRequest;
import com.da.usercenter.model.request.DeleteFriendRequest;
import com.da.usercenter.model.request.UpdateTagRequest;
import com.da.usercenter.model.vo.UserVO;

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
    Long userRegister(String loginAccount, String loginPassword, String checkPassword, String nickname);

    /**
     * 用户登录
     *
     * @param loginAccount  账号
     * @param loginPassword 密码
     * @param request 客户端请求对象
     * @return 脱敏后的用户信息
     */
    User userLogin(String loginAccount, String loginPassword, HttpServletRequest request);

    /**
     * 查询用户
     * @param nickName 昵称
     * @param request 客户端请求对象
     * @return 用户信息
     */
    List<User> searchUser(String nickName, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param user 用户信息
     * @param request 客户端请求对象
     * @return 成功 or 失败
     */
    boolean deleteUser(User user, HttpServletRequest request);

    /**
     * 管理员验证
     * @param request 客户端请求对象
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     *
     * @param loginUser 登录用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 用户脱敏
     * @param user 用户全量信息
     * @return 安全用户
     */
    User getSafeUser(User user);

    /**
     * 获取当前用户信息
     * @param request 客户端请求对象
     * @return 当前用户信息
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 注销
     * @param request 客户端请求对象
     * @return 1-注销成功
     */
    Boolean userLogOut(HttpServletRequest request);


    /**
     * 根据标签搜索用户
     */
    List<User> searchUsersByTags(List<String> tags);



    /**
     * 更新用户信息
     *
     * @param user 更新用户信息
     * @return 是否更新成功
     */
    Boolean updateUser(User user, HttpServletRequest request);

    /**
     * 推荐用户
     * @param request 客户端请求对象
     * @return 推荐用户分页对象
     */
    Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);


    /**
     * 获取最匹配的用户
     * @param num 推荐数量
     * @param request 客户端请求对象
     * @return 用户信息
     */
    List<User> matchUsers(long num, HttpServletRequest request);

    /**
     * 获取好友列表
     * @param request
     * @return
     */
    List<UserVO> getFriends(HttpServletRequest request);

    /**
     * 添加好友
     * @param addFriendRequest
     * @param request
     * @return
     */
    Boolean addFriend(AddFriendRequest addFriendRequest, HttpServletRequest request);

    /**
     * 删除好友
     * @param deleteFriendRequest
     * @param request
     * @return
     */
    Boolean deleteFriend(DeleteFriendRequest deleteFriendRequest, HttpServletRequest request);

    /**
     * 更新标签
     * @param updateTagRequest
     * @param request
     * @return
     */
    Boolean updateTag(UpdateTagRequest updateTagRequest, HttpServletRequest request);
}

