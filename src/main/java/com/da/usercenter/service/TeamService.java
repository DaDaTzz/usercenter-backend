package com.da.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.model.dto.TeamQuery;
import com.da.usercenter.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.model.request.CreateTeamRequest;
import com.da.usercenter.model.request.TeamJoinRequest;
import com.da.usercenter.model.request.TeamUpdateRequest;
import com.da.usercenter.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 达
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-09-21 17:03:10
 */
public interface TeamService extends IService<Team> {

    Long createTeam(CreateTeamRequest createTeamRequest, HttpServletRequest request);


    Boolean deleteTeam(long teamId);


    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    Team getTeamById(long teamId);

    List<TeamUserVO> getTeamList(TeamQuery teamQuery, HttpServletRequest request);

    Page<Team> getTeamListByPage(TeamQuery teamQuery);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);
}
