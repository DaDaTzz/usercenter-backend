package com.da.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import jdk.nashorn.internal.objects.annotations.ScriptClass;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 每天执行，预热推荐用户
     */
    @Scheduled(cron = "0 10 13 ? * *") // 每天9:42执行
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("precachejob:doCache:lock");
        try {
            // 只有一个线程能抢到锁，执行定时任务
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                for (Long userId : mainUserList) {
                    // 将读到的数据存入缓存中
                    System.out.println("缓存预热定时任务执行！！！！！！！！！！！！！！！！！！");
                    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = "user:recommend:" + userId;
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(redisKey, userPage, 30, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        }finally {
            // 只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }


    }
}
