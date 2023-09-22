package com.da.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.model.dto.TeamQuery;
import com.da.usercenter.model.entity.Team;
import com.da.usercenter.model.request.CreateTeamRequest;
import com.da.usercenter.model.request.TeamJoinRequest;
import com.da.usercenter.model.request.TeamUpdateRequest;
import com.da.usercenter.model.vo.TeamUserVO;
import com.da.usercenter.service.TeamService;
import com.da.usercenter.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍接口
 *
 * @author 达
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    public ResponseResult<Long> createTeam(@RequestBody CreateTeamRequest createTeamRequest, HttpServletRequest request) {
        Long teamId = teamService.createTeam(createTeamRequest, request);
        return ResponseResult.success(teamId);
    }

    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteTeam(long teamId) {
        Boolean res = teamService.deleteTeam(teamId);
        return ResponseResult.success(res);
    }

    @PostMapping("/update")
    public ResponseResult<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        Boolean res = teamService.updateTeam(teamUpdateRequest, request);
        return ResponseResult.success(res);
    }

    @GetMapping("/query")
    public ResponseResult<Team> getTeamById(long teamId) {
        Team team = teamService.getTeamById(teamId);
        return ResponseResult.success(team);
    }

    @GetMapping("/list")
    public ResponseResult<List<TeamUserVO>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery, request);
        return ResponseResult.success(teamUserVOList);
    }

    @GetMapping("/list/page")
    public ResponseResult<Page<Team>> getTeamListByPage(TeamQuery teamQuery) {
        Page<Team> teamPage = teamService.getTeamListByPage(teamQuery);
        return ResponseResult.success(teamPage);
    }

    @PostMapping("/join")
    public ResponseResult<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        Boolean res = teamService.joinTeam(teamJoinRequest, request);
        return ResponseResult.success(res);
    }




}
