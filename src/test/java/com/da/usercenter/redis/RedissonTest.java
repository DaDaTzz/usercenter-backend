package com.da.usercenter.redis;


import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        // list
        RList<Object> rList = redissonClient.getList("test-list");
        rList.add("dada");
        rList.remove(0);
        // map
        RMap<Object, Object> rMap = redissonClient.getMap("test-map");
        rMap.put("name", "da");
        System.out.println(rMap.get("name"));
        rMap.remove("name");
    }
}
