package com.da.usercenter.interceptor;

import cn.hutool.core.util.StrUtil;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.utils.TokenUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;


public class RefreshLoginStatusInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public RefreshLoginStatusInterceptor(RedisTemplate redisTemplate ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //System.out.println("登录过期时间已经刷新。。。。。。。。");
        // 1.获取请求头中的token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 2.基于 token 获取 redis 中的用户
        String userId = TokenUtils.getAccount(token);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        User user = (User)valueOperations.get("user:login:" + userId);
        valueOperations.set("user:login:" + userId, user, 30, TimeUnit.MINUTES);
        // 8.放行
        return true;
    }

}
