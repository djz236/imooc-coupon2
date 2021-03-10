package com.imooc.coupon.service.impl;

import com.google.common.base.Stopwatch;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 异步服务接口实现
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {
    /**
     * CouponTemplate   Dao接口
     */
    private final CouponTemplateDao templateDao;
    /**
     * 注入Redis 模板
     */
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public AsyncServiceImpl(CouponTemplateDao templateDao, StringRedisTemplate redisTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据模板异步的创建优惠券码
     *
     * @param template
     */
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        //StopWatch是位于org.springframework.util包下的一个工具类，
        // 通过它可方便的对程序部分代码进行计时(ms级别)，适用于同步单线程代码块。
        Stopwatch stopwatch = Stopwatch.createStarted();
        Set<String> couponCodes = buildCouponCode(template);
        String redisKey=String.format("%s%s",
                Constant.RedisPrefix.COUPON_TEMPLATE,
                template.getId().toString());
        /**redisTemplate.opsForValue();//操作字符串
        redisTemplate.opsForHash();//操作hash
        redisTemplate.opsForList();//操作list
        redisTemplate.opsForSet();//操作set
        redisTemplate.opsForZSet();//操作有序set*/
        // rightPushAll 将所有指定的值插入存储在键的列表的头部。
        // 如果键不存在，则在执行推送操作之前将其创建为空列表。（从右边插入）
        log.info("Push CouponCode To Redis:{}",
                redisTemplate.opsForList().rightPushAll(redisKey,couponCodes));
        template.setAvailable(true);
        templateDao.save(template);
        stopwatch.stop();
        log.info("Construct CouponCode By Template Cost:{}ms",
                stopwatch.elapsed(TimeUnit.SECONDS));
        //TODO 发送短信或邮件通知优惠券模板已经可用
        log.info("CouponTemplate({}) is available!",template.getId());

    }

    private Set<String> buildCouponCode(CouponTemplate template) {
        // 创建自动start的计时器
        Stopwatch stopwatch = Stopwatch.createStarted();
        Set<String> result = new HashSet<>(template.getCount());
        //前4位
        String prefix4 = template.getProductLine().getCode().toString()
                + template.getCategory().getCode();
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreateTime());
        for (int i = 0; i != template.getCount(); i++) {
            result.add(prefix4 + buildCouponCodeSuffixl4(date));
        }
        assert result.size()==template.getCount();
        stopwatch.stop();
        log.info("Build Coupon Code Cost:{}ms",stopwatch.elapsed(TimeUnit.SECONDS));
        return result;
    }

    /**
     * 构造优惠券码的后14位
     *
     * @param date
     * @return
     */
    private String buildCouponCodeSuffixl4(String date) {
        char[] bases = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};
        //中间六位
        List<Character> chars = date.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        Collections.shuffle(chars);//使用默认随机源对列表进行置换，所有置换发生的可能性都是大致相等的。
        String mid6 = chars.stream().map(Objects::toString).collect(Collectors.joining());
        //后八位
        String suffix8=
                RandomStringUtils.random(1,bases)
                +RandomStringUtils.randomNumeric(7);
        String s = mid6+suffix8;
        return s;
    }
}
