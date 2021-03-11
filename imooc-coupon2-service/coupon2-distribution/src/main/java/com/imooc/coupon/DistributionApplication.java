package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;

/**
 * 分发系统的启动入口
 */
@EnableJpaAuditing//jpa的审计功能 数据访问等
@EnableFeignClients//涉及到调用其他微服务
@EnableCircuitBreaker//熔断降级 断路器
@EnableEurekaClient//eureka 注册中心
@SpringBootApplication
public class DistributionApplication {
    @Bean
    @LoadBalanced
//负载均衡
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class, args);
    }
}
