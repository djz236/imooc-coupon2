package com.imooc.coupon.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 消息转换器
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    // 添加converter的第二种方式
    // 通常在只有一个自定义WebMvcConfigurerAdapter时，会把这个方法里面添加的converter(s)依次放在最高优先级（List的头部）
    // 虽然第一种方式的代码先执行，但是bean的添加比这种方式晚，所以方式二的优先级 大于 方式一
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.clear();
        // add方法可以指定顺序，有多个自定义的WebMvcConfigurerAdapter时，可以改变相互之间的顺序
        // 但是都在springmvc内置的converter前面
        converters.add(new MappingJackson2HttpMessageConverter());
    }
}
