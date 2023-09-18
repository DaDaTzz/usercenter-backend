package com.da.usercenter;

import cn.hutool.core.lang.Assert;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setNickname("花无缺");
        user.setSex(0);
        user.setStates(0);
        user.setType(0);
        user.setLoginAccount("666888");
        user.setLoginPassword("12345678");
        user.setPhone("18370598888");
        user.setEmail("123@qq.com");
        user.setProfilePhoto("https://z1.ax1x.com/2023/06/11/pCVNPyD.jpg");
        user.setProfile("我是一名程序员，^_^O(∩_∩)O哈哈~");
        userMapper.insert(user);
    }

    @Test
    public void testUpdateUserById(){
        User user = new User();
        user.setId(1);
        user.setSex(1);
        user.setNickname("Daaaaaaa");
        userMapper.updateById(user);
    }

    @Test
    public void testGetUserById(){
        User user = userMapper.selectById(1);
        System.out.println(user);
    }

    @Test
    public void testSearchUsersByTags(){
        List<String> tags = Arrays.asList("python","java");
        List<User> users = userService.searchUsersByTags(tags);
        System.out.println(users);
        System.out.println("size = " + users.size());
    }


}
