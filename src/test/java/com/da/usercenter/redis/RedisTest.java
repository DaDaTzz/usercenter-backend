package com.da.usercenter.redis;

import com.da.usercenter.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();

        // 增
        valueOperations.set("name", "da");
        valueOperations.set("age", 18);
        valueOperations.set("height", 1.8);
        User user = new User();
        user.setId(1);
        user.setNickname("哒哒");
        valueOperations.set("user", user);

        // 查
        Object name = valueOperations.get("name");
        Object age = valueOperations.get("age");
        Object height = valueOperations.get("height");
        User u = (User)valueOperations.get("user");

        Assertions.assertEquals(name, "da");
        Assertions.assertEquals(age, 18);
        Assertions.assertEquals(height, 1.8);
        Assertions.assertEquals(u.getId(), 1);
        Assertions.assertEquals(u.getNickname(), "哒哒");


        // 改
        valueOperations.set("name", "dada");
        Assertions.assertEquals(valueOperations.get("name"), "dada");

        // 删
        if(redisTemplate.hasKey("age")){
            redisTemplate.delete("age");
        }
        Assertions.assertEquals(redisTemplate.hasKey("age"), false);

    }
}
