package com.da.usercenter.algorithm;

import com.da.usercenter.utils.AlgorithmUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AlgorithmTest {

    @Test
    public void test(){
        List<String> l1 = Arrays.asList("java", "男", "小学");
        List<String> l2 = Arrays.asList("java","男","大学");
        List<String> l3 = Arrays.asList("c++","女","初中");
        int i1 = AlgorithmUtil.minDistance(l1, l2);
        int i2 = AlgorithmUtil.minDistance(l1, l3);
        System.out.println(i1);
        System.out.println(i2);
    }
}
