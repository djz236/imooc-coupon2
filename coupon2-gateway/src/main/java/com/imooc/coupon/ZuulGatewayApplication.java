package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1. @EnableZuulProxy 标识当前的应用是 Zuul Server
 * 2. @SpringCloudApplication 组合了 SpringBoot 应用 + 服务发现 + 熔断
 */
@EnableZuulProxy
@EnableFeignClients// 一定要加入，因为 permission-sdk 中 写了 Feign 接口
@SpringCloudApplication
public class ZuulGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class,args);
    }
}
