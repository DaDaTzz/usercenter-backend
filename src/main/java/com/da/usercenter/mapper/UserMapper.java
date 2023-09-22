package com.da.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.da.usercenter.model.entity.User;

import java.util.List;


/**
 * (User)表数据库访问层
 *
 * @author Da
 * @since 2023-06-10 13:24:17
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据队伍id查询已加入的用户信息
     * @param teamId
     * @return
     */
    List<User> getUserListByTeamId(long teamId);
}

