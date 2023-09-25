package com.da.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.model.dto.TeamQuery;
import com.da.usercenter.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.da.usercenter.model.request.*;
import com.da.usercenter.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 达
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param createTeamRequest 创建队伍请求对象
     * @param request 客户端请求对象
     * @return 创建成功的队伍 id
     */
    Long createTeam(CreateTeamRequest createTeamRequest, HttpServletRequest request);


    /**
     * 删除队伍
     * @param teamId 队伍 id
     * @return 是否删除成功
     */
    Boolean deleteTeam(long teamId);


    /**
     * 更新队伍信息
     * @param teamUpdateRequest 更新队伍请求对象
     * @param request 客户端请求对象
     * @return 是否更新成功
     */
    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    /**
     * 通过队伍 id 获取队伍信息
     * @return 队伍信息
     */
    Team getTeamById(long id);

    /**
     * 查询符合要求的队伍信息
     * @param teamQuery 队伍信息传输对象
     * @param request 客户端请求对象
     * @return 队伍信息以及所加入该队伍用户信息
     */
    List<TeamUserVO> getTeamList(TeamQuery teamQuery, HttpServletRequest request);

    /**
     * 查询所有队伍信息（分页查询）
     * @param teamQuery 队伍信息传输对象
     * @return 队伍list
     */
    Page<Team> getTeamListByPage(TeamQuery teamQuery);

    /**
     * 加入队伍
     * @param teamJoinRequest 加入队伍请求对象
     * @param request 客户端请求对象
     * @return 是否加入成功
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    /**
     * 退出队伍
     * @param teamExitRequest 退出队伍请求对象
     * @param request 客户端请求对象
     * @return 是否退出成功
     */
    Boolean exitTeam(TeamExitRequest teamExitRequest, HttpServletRequest request);

    /**
     * 解散队伍
     * @param teamDisband 解散队伍请求对象
     * @param request 客户端请求对象
     * @return 是否解散成功
     */
    Boolean disbandTeam(TeamDisband teamDisband, HttpServletRequest request);

    /**
     * 获取当前用户创建的队伍信息
     * @param request 客户端请求对象
     * @return teamList
     */
    List<TeamUserVO> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request);

    /**
     * 获取当前用户加入的队伍信息
     * @param request
     * @return
     */
    List<TeamUserVO> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request);
}
