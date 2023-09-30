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


/**
 * 刷新登录有效时间拦截器
 */
public class RefreshLoginStatusInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public RefreshLoginStatusInterceptor(RedisTemplate redisTemplate ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //System.out.println("登录过期时间已经刷新。。。。。。。。");
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        String userId = TokenUtils.getAccount(token);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 如果能获取到用户信息，说明已登录并且未过期，刷新过期时间
        User user = (User)valueOperations.get("user:login:" + userId);
        if(user != null){
            valueOperations.set("user:login:" + userId, user, 30, TimeUnit.MINUTES);
        }
        // 放行
        return true;
    }

}
