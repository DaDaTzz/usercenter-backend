package com.da.usercenter;

import cn.hutool.core.date.StopWatch;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    @Test
    public void testInsertUser(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int batchSize = 100000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            Random random = new Random();
            List<User> userList = new ArrayList<>();

            while(true){
                j++;
                User user = new User();
                user.setNickname("Da");
                user.setSex(1);
                user.setStates(0);
                user.setType(0);
                user.setLoginAccount("666888" + random.nextInt());
                user.setLoginPassword("12345678");
                user.setPhone("18370598888");
                user.setEmail("123@qq.com");
                user.setTags("[]");
                user.setProfilePhoto("https://z1.ax1x.com/2023/06/11/pCVNPyD.jpg");
                user.setProfile("我是一名程序员，^_^O(∩_∩)O哈哈~");
                userList.add(user);
                if(j % batchSize == 0){
                    break;
                }
            }

            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                userService.saveBatch(userList, batchSize);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());



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

    @Test
    public void testGson(){
        String str = "['java','python','c','c++']";
        Gson gson = new Gson();
        List list = gson.fromJson(str, List.class);
        list.forEach(i ->{
            System.out.println(i);
        });
    }

    @Test
    public void testGetUserListByTeamId(){
        List<User> userList = userMapper.getUserListByTeamId(27);
        userList.forEach(user -> {
            System.out.println(user);
        });
    }

}
