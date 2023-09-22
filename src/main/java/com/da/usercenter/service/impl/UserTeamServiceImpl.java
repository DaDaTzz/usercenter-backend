package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.model.entity.UserTeam;
import com.da.usercenter.service.UserTeamService;
import com.da.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 达
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2023-09-21 17:07:21
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




