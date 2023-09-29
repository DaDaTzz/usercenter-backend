package com.da.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.dto.TeamQuery;
import com.da.usercenter.model.entity.Team;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.entity.UserTeam;
import com.da.usercenter.model.enums.TeamStatesEnum;
import com.da.usercenter.model.request.*;
import com.da.usercenter.model.vo.TeamUserVO;
import com.da.usercenter.model.vo.UserVO;
import com.da.usercenter.service.TeamService;
import com.da.usercenter.mapper.TeamMapper;
import com.da.usercenter.service.UserService;
import com.da.usercenter.service.UserTeamService;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.da.usercenter.common.ErrorCode.*;

/**
 * 队伍 Service
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedissonClient redissonClient;


    /**
     * 创建队伍
     *
     * @param createTeamRequest 创建队伍请求参数
     * @param request           客户端请求对象
     * @return 创建成功的队伍 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTeam(CreateTeamRequest createTeamRequest, HttpServletRequest request) {
        // 1.请求参数是否为空
        if (createTeamRequest == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 2.是否登录
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 加锁，
        long userId = loginUser.getId();
        // 3.校验信息
        // 3.1 队伍人数 > 1 切 < 20
        int maxNum = Optional.ofNullable(createTeamRequest.getMaxNum()).orElse(1);
        if (maxNum < 0 || maxNum > 20) {
            throw new BusinessException(PARAMS_ERROR, "队伍人数不满足要求");
        }
        // 3.2 队伍标题 <= 20
        String name = createTeamRequest.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(PARAMS_ERROR, "队伍标题不满足要求");
        }
        // 3.3 描述 <= 512
        String description = createTeamRequest.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(PARAMS_ERROR, "队伍描述过长");
        }
        // 3.4 states 是否公开 （int） 不传默认为0 公开
        int states = Optional.ofNullable(createTeamRequest.getStates()).orElse(0);
        TeamStatesEnum teamStatesEnum = TeamStatesEnum.getEnumByValue(states);
        if (teamStatesEnum == null) {
            throw new BusinessException(PARAMS_ERROR, "队伍状态异常");
        }
        // 3.5 如果 states 是加密状态，一定要有密码，且密码 <= 32
        String password = createTeamRequest.getPassword();
        if (teamStatesEnum.equals(TeamStatesEnum.SECRET)) {
            if (StringUtils.isBlank(password)) {
                throw new BusinessException(PARAMS_ERROR, "队伍加密必须设置密码");
            }
            if (password.length() > 32) {
                throw new BusinessException(PARAMS_ERROR, "队伍密码必须小于等于32位");
            }
        }
        if (teamStatesEnum.equals(TeamStatesEnum.PUBLIC) && StringUtils.isNotBlank(password)) {
            throw new BusinessException(PARAMS_ERROR, "公开队伍不允许设置密码");
        }
        // 3.6 超时时间需要 > 当前时间
        Date expireTime = createTeamRequest.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(PARAMS_ERROR, "队伍超时时间异常");
        }
        // 3.7 校验用户最多创建和加入5个队伍
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        int hasTeamNum = userTeamService.count(userTeamLambdaQueryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(PARAMS_ERROR, "最多创建和加入5支队伍");
        }
        // 3.8 同一个用户创建的队伍名是否相同
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId, userId).eq(Team::getName, name);
        List<Team> teamList = this.list(teamLambdaQueryWrapper);
        if (!teamList.isEmpty()) {
            throw new BusinessException(PARAMS_ERROR, "该用户已存在同名队伍");
        }
        // 4. 插入队伍信息到队伍表
        createTeamRequest.setUserId(userId);
        Team team = new Team();
        BeanUtils.copyProperties(createTeamRequest, team);
        System.out.println(team);
        boolean res = this.save(team);
        if (!res) {
            throw new BusinessException(SYSTEM_ERROR, "信息表插入数据失败");
        }
        Long teamId = team.getId();
        // 5. 插入 用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUser.getId());
        userTeam.setJoinTime(new Date());
        res = userTeamService.save(userTeam);
        if (!res) {
            throw new BusinessException(SYSTEM_ERROR, "用户关系表插入数据失败");
        }
        return teamId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        boolean res = this.removeById(teamId);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        LambdaQueryWrapper<UserTeam> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        res = userTeamService.remove(teamLambdaQueryWrapper);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除用户队伍关系失败");
        }
        return true;
    }

    @Override
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 判断队伍是否存在
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        if (this.getById(teamUpdateRequest.getId()) == null) {
            throw new BusinessException(PARAMS_ERROR, "队伍不存在");
        }
        // 判断是否为管理员或自己
        long loginUserId = userService.getCurrentUser(request).getId();
        Team team = this.getById(teamUpdateRequest);
        if (!userService.isAdmin(request) && team.getUserId() != loginUserId) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 如果 states = 0 公开队伍，清除密码
        TeamStatesEnum teamStatesEnum = TeamStatesEnum.getEnumByValue(teamUpdateRequest.getStates());
        if (teamStatesEnum.equals(TeamStatesEnum.PUBLIC)) {
            teamUpdateRequest.setPassword("");
        }
        // 如果states = 2 加密队伍，必须设置密码
        if (teamStatesEnum.equals(TeamStatesEnum.SECRET) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
            throw new BusinessException(PARAMS_ERROR, "加密状态必须设置密码");
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        boolean res = this.updateById(updateTeam);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍信息失败");
        }
        return true;
    }

    @Override
    public Team getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(PARAMS_ERROR, "用户不存在");
        }
        return team;
    }

    @Override
    public List<TeamUserVO> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if (teamQuery != null) {
            // 根据队伍名称查询
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like(Team::getName, name);
            }
            String searchText = teamQuery.getSearchText();
            // 根据关键词同时对队伍名称和描述进行搜索
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like(Team::getName, searchText).or().like(Team::getDescription, searchText));
            }
            // 根据描述查询
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like(Team::getDescription, description);
            }
            // 根据队伍id查询
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq(Team::getId, id);
            }
            // 根据队长id查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq(Team::getUserId, userId);
            }
            // 根据最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq(Team::getMaxNum, maxNum);
            }
            // 根据状态查询（公开房间所有人可以查看，只有管理员才能查看加密的房间，私有房间只允许自己查看）
            Integer states = teamQuery.getStates();
            if (states != null && states > -1) {
                TeamStatesEnum teamStatesEnum = TeamStatesEnum.getEnumByValue(states);
                // 管理员可以查看所有队伍
                if (userService.isAdmin(request)) {
                    queryWrapper.eq(Team::getStates, states);
                    // 非管理员可以查看所有公开队伍
                } else if (!userService.isAdmin(request) && teamStatesEnum.equals(TeamStatesEnum.PUBLIC)) {
                    queryWrapper.eq(Team::getStates, states);
                    // 非管理员只允许查看自己的私有队伍
                } else if (!userService.isAdmin(request) && teamStatesEnum.equals(TeamStatesEnum.PRIVATE)) {
                    User loginUser = userService.getCurrentUser(request);
                    queryWrapper.eq(Team::getStates, states).eq(Team::getUserId, loginUser.getId());
                    // 非管理员允许查看加密房间
                } else if (!userService.isAdmin(request) && teamStatesEnum.equals(TeamStatesEnum.SECRET)) {
                    queryWrapper.eq(Team::getStates, states);
                }
            }
        }
        // 不展示过期队伍
        queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date()).or().isNull(Team::getExpireTime));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        // 关联查询创建队伍的用户
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            userQueryWrapper.eq(User::getId, userId);
            User user = userService.getById(userId);
            UserVO userVO = new UserVO();
            // 脱敏用户信息
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
            }
            // 脱敏队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            teamUserVO.setCreateUser(userVO);
            // 获取已加入队伍的用户信息
            Long teamId = team.getId();
            List<User> joinUsers = userMapper.getUserListByTeamId(teamId);
            ArrayList<UserVO> userVOS = new ArrayList<>();
            if (!joinUsers.isEmpty()) {
                for (User joinUser : joinUsers) {
                    UserVO u = new UserVO();
                    BeanUtils.copyProperties(joinUser, u);
                    userVOS.add(u);
                }
            }
            teamUserVO.setJoinUsers(userVOS);
            teamUserVO.setHasJoinNum(joinUsers.size());
            teamUserVOList.add(teamUserVO);
        }
        // 判断当前用户是否已加入队伍
        List<Long> teamIdList = teamUserVOList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        try {
            User loginUser = userService.getCurrentUser(request);
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
            userTeamLambdaQueryWrapper.in(UserTeam::getTeamId, teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamUserVOList.forEach(teamUserVO -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(teamUserVO.getId());
                teamUserVO.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }


        return teamUserVOList;
    }

    @Override
    public Page<Team> getTeamListByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        return this.page(new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize()), queryWrapper);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍请求参数
     * @param request         客户端请求对象
     * @return 是否加入成功 true or false
     */
    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        // 是否登录
        if (currentUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        Long teamId = teamJoinRequest.getTeamId();
        long userId = currentUser.getId();
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 队伍是否存在
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(PARAMS_ERROR, "队伍不存在");
        }

        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("join_team");
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
                    int joinTeamCounts = userTeamService.count(userTeamLambdaQueryWrapper);
                    // 每个用户只能加入5个队伍
                    if (joinTeamCounts >= 5) {
                        throw new BusinessException(PARAMS_ERROR, "用户最多创建和加入5个队伍");
                    }
                    // 只能加入未满、未过期的队伍
                    userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
                    int hasUserNum = userTeamService.count(userTeamLambdaQueryWrapper);
                    Integer teamMaxNum = team.getMaxNum();
                    if (hasUserNum >= teamMaxNum) {
                        throw new BusinessException(PARAMS_ERROR, "队伍已满");
                    }
                    if (team.getExpireTime() != null) {
                        if (team.getExpireTime().getTime() < new Date().getTime()) {
                            throw new BusinessException(PARAMS_ERROR, "队伍已过期");
                        }
                    }
                    // 不能重复加入已加入的队伍
                    userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
                    userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
                    UserTeam one = userTeamService.getOne(userTeamLambdaQueryWrapper);
                    if (one != null) {
                        throw new BusinessException(PARAMS_ERROR, "已加入该队伍");
                    }
                    // 禁止加入私有的队伍
                    TeamStatesEnum teamStatesEnum = TeamStatesEnum.getEnumByValue(team.getStates());
                    if (TeamStatesEnum.PRIVATE.equals(teamStatesEnum)) {
                        throw new BusinessException(PARAMS_ERROR, "私有队伍不能加入");
                    }
                    // 如果加入的队伍是加密的，必须密码匹配
                    if (TeamStatesEnum.SECRET.equals(teamStatesEnum) && StringUtils.isBlank(teamJoinRequest.getPassword())) {
                        throw new BusinessException(PARAMS_ERROR, "请输入队伍密码");
                    }
                    if (TeamStatesEnum.SECRET.equals(teamStatesEnum) && StringUtils.isNotBlank(teamJoinRequest.getPassword())) {
                        String password = team.getPassword();
                        if (!teamJoinRequest.getPassword().equals(password)) {
                            throw new BusinessException(PARAMS_ERROR, "队伍密码错误");
                        }
                    }
                    // 插入数据
                    UserTeam userTeam = new UserTeam();
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    userTeam.setUserId(userId);
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("joinTeam error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    /**
     * 退出队伍
     *
     * @param teamExitRequest 退出队伍请求参数
     * @param request         客户端请求对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean exitTeam(TeamExitRequest teamExitRequest, HttpServletRequest request) {
        // 校验参数
        if (teamExitRequest == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        // 登录
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        // 队伍是否存在
        Long teamId = teamExitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(PARAMS_ERROR, "队伍不存在");
        }
        // 用户是否在该队伍中
        long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        UserTeam userTeam = userTeamService.getOne(userTeamLambdaQueryWrapper);
        if (userTeam == null) {
            throw new BusinessException(PARAMS_ERROR, "用户不在此队伍中");
        }

        userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 队伍仅剩1人，队伍解散
        if (userTeamList.size() <= 1) {
            // 删除用户队伍关系表数据
            userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
            boolean res = userTeamService.remove(userTeamLambdaQueryWrapper);
            if (!res) {
                throw new BusinessException(DATABASE_ERROR, "删除用户队伍关系表数据失败");
            }
            // 删除队伍表数据
            teamLambdaQueryWrapper.eq(Team::getId, teamId);
            teamLambdaQueryWrapper.eq(Team::getUserId, userId);
            return this.remove(teamLambdaQueryWrapper);
        }

        teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId, userId);
        teamLambdaQueryWrapper.eq(Team::getId, teamId);
        Team isCaptain = this.getOne(teamLambdaQueryWrapper);
        // 若队长退出，并且队伍人数大于1
        if (isCaptain != null && userTeamList.size() > 1) {
            // 删除用户队伍关系表数据
            userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
            boolean res = userTeamService.remove(userTeamLambdaQueryWrapper);
            if (!res) {
                throw new BusinessException(DATABASE_ERROR, "删除用户队伍关系表数据失败");
            }
            // 找出第二早加入的用户，修改队伍表信息，转移队长权限
            userTeamList.sort(Comparator.comparing(UserTeam::getJoinTime));
            Long secondJoinUserId = userTeamList.get(1).getUserId();
            team.setUserId(secondJoinUserId);
            return this.updateById(team);
        }

        // 若不是队长，直接删除用户队伍关系表数据
        userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        return userTeamService.remove(userTeamLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disbandTeam(TeamDisband teamDisband, HttpServletRequest request) {
        if (teamDisband == null) {
            throw new BusinessException(NULL_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        // 是否登录
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        // 队伍是否存在
        Long teamId = teamDisband.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(PARAMS_ERROR, "队伍不存在");
        }
        // 是否为队长
        long userId = userService.getCurrentUser(request).getId();
        if (team.getUserId() != userId) {
            throw new BusinessException(NO_AUTH, "非队长不允许解散队伍");
        }
        // 删除用户队伍关系表数据
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        boolean res = userTeamService.remove(userTeamLambdaQueryWrapper);
        if (!res) {
            throw new BusinessException(DATABASE_ERROR, "用户队伍关系表数据删除失败");
        }
        // 删除队伍表数据
        return this.removeById(teamId);

    }

    @Override
    public List<TeamUserVO> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        if (loginUser.getId() <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId, loginUser.getId());
        // 实现队伍名称模糊匹配
        if (StringUtils.isNotBlank(teamQuery.getSearchText())) {
            String searchText = teamQuery.getSearchText();
            teamLambdaQueryWrapper.like(Team::getName, searchText);
        }
        List<Team> teamList = this.list(teamLambdaQueryWrapper);


        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        // 关联查询创建队伍的用户
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            userQueryWrapper.eq(User::getId, userId);
            User user = userService.getById(userId);
            UserVO userVO = new UserVO();
            // 脱敏用户信息
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
            }
            // 脱敏队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            teamUserVO.setCreateUser(userVO);
            // 获取已加入队伍的用户信息
            Long teamId = team.getId();
            List<User> joinUsers = userMapper.getUserListByTeamId(teamId);
            ArrayList<UserVO> userVOS = new ArrayList<>();
            if (!joinUsers.isEmpty()) {
                for (User joinUser : joinUsers) {
                    UserVO u = new UserVO();
                    BeanUtils.copyProperties(joinUser, u);
                    userVOS.add(u);
                }
            }
            teamUserVO.setJoinUsers(userVOS);
            teamUserVO.setHasJoinNum(joinUsers.size());
            teamUserVOList.add(teamUserVO);
        }

        // 判断当前用户是否已加入队伍
        List<Long> teamIdList = teamUserVOList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        try {
            loginUser = userService.getCurrentUser(request);
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
            userTeamLambdaQueryWrapper.in(UserTeam::getTeamId, teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamUserVOList.forEach(teamUserVO -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(teamUserVO.getId());
                teamUserVO.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        return teamUserVOList;
    }

    @Override
    public List<TeamUserVO> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        long userId = loginUser.getId();
        if (userId <= 0) {
            throw new BusinessException(PARAMS_ERROR);
        }
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
        if (userTeamList.isEmpty()) {
            return new ArrayList<>();
        }
        Iterator<UserTeam> iterator = userTeamList.iterator();

        while (iterator.hasNext()) {
            UserTeam userTeam = iterator.next();
            Long teamId = userTeam.getTeamId();
            teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            teamLambdaQueryWrapper.eq(Team::getId, teamId);
            Team team = this.getOne(teamLambdaQueryWrapper);
            if (Objects.equals(team.getId(), userTeam.getTeamId()) && Objects.equals(team.getUserId(), userTeam.getUserId())) {
                iterator.remove();
            }
        }

        ArrayList<Team> teams = new ArrayList<>();
        for (UserTeam userTeam : userTeamList) {
            teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            teamLambdaQueryWrapper.eq(Team::getId, userTeam.getTeamId());
            Team team = this.getOne(teamLambdaQueryWrapper);
            teams.add(team);
        }

        // 实现队伍名称模糊匹配
        if (StringUtils.isNotBlank(teamQuery.getSearchText())) {
            ArrayList<TeamUserVO> teamUserVOS = new ArrayList<>();
            for (Team team : teams) {
                if (team.getName().contains(teamQuery.getSearchText())) {
                    Long uId = team.getUserId();
                    if (uId == null) {
                        break;
                    }
                    LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
                    userQueryWrapper.eq(User::getId, userId);
                    User user = userService.getById(userId);
                    UserVO userVO = new UserVO();
                    // 脱敏用户信息
                    if (user != null) {
                        BeanUtils.copyProperties(user, userVO);
                    }
                    // 脱敏队伍信息
                    TeamUserVO teamUserVO = new TeamUserVO();
                    BeanUtils.copyProperties(team, teamUserVO);
                    teamUserVO.setCreateUser(userVO);
                    // 获取已加入队伍的用户信息
                    Long teamId = team.getId();
                    List<User> joinUsers = userMapper.getUserListByTeamId(teamId);
                    ArrayList<UserVO> userVOS = new ArrayList<>();
                    if (!joinUsers.isEmpty()) {
                        for (User joinUser : joinUsers) {
                            UserVO u = new UserVO();
                            BeanUtils.copyProperties(joinUser, u);
                            userVOS.add(u);
                        }
                    }
                    teamUserVO.setJoinUsers(userVOS);
                    teamUserVOS.add(teamUserVO);
                }
            }
            return teamUserVOS;
        }

        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        // 关联查询创建队伍的用户
        for (Team team : teams) {
            Long uId = team.getUserId();
            if (uId == null) {
                continue;
            }
            userQueryWrapper.eq(User::getId, userId);
            User user = userService.getById(userId);
            UserVO userVO = new UserVO();
            // 脱敏用户信息
            if (user != null) {
                BeanUtils.copyProperties(user, userVO);
            }
            // 脱敏队伍信息
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            teamUserVO.setCreateUser(userVO);
            // 获取已加入队伍的用户信息
            Long teamId = team.getId();
            List<User> joinUsers = userMapper.getUserListByTeamId(teamId);
            ArrayList<UserVO> userVOS = new ArrayList<>();
            if (!joinUsers.isEmpty()) {
                for (User joinUser : joinUsers) {
                    UserVO u = new UserVO();
                    BeanUtils.copyProperties(joinUser, u);
                    userVOS.add(u);
                }
            }
            teamUserVO.setJoinUsers(userVOS);
            teamUserVO.setHasJoinNum(joinUsers.size());
            teamUserVOList.add(teamUserVO);
        }

        // 判断当前用户是否已加入队伍
        List<Long> teamIdList = teamUserVOList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        try {
            loginUser = userService.getCurrentUser(request);
            userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
            userTeamLambdaQueryWrapper.in(UserTeam::getTeamId, teamIdList);
            List<UserTeam> UTList = userTeamService.list(userTeamLambdaQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = UTList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamUserVOList.forEach(teamUserVO -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(teamUserVO.getId());
                teamUserVO.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }

        return teamUserVOList;
    }


}




