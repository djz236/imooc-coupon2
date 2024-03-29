package com.imooc.coupon;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 为了引入 permissionSDK 而添加的配置类
 */
@Configuration
@EnableFeignClients
public class PermissionSDKConfig {
}
