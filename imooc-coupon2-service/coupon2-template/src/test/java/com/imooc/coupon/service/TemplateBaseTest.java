package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TemplateBaseTest {
    @Autowired
    private ITemplateBaseService baseService;
    @Test
    public void testbuildTemplate() throws Exception{
        System.out.println(JSON.toJSON(baseService.buildTemplateInfo(17)));
        System.out.println(JSON.toJSON(baseService.buildTemplateInfo(18)));
    }
}
