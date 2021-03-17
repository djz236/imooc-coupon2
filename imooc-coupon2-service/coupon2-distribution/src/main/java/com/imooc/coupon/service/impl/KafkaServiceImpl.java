package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.service.IKafkaService;
import com.imooc.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Kafka 相关的服务接口实现
 * 核心思想：是将 Cache 中的 Coupon 的状态变化同步到 DB中
 */
@Slf4j
@Component
public class KafkaServiceImpl implements IKafkaService {
    /**
     * coupon dao接口
     *
     * @param record
     */
    private final CouponDao couponDao;

    @Autowired
    public KafkaServiceImpl(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    /**
     * 消费优惠卷 Kafka 消息
     *
     * @param record
     */
    @Override
    @KafkaListener(topics = {Constant.TOPIC},
            groupId = "imooc-coupon-1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            CouponKafkaMessage couponInfo = JSON.parseObject(message.toString(),
                    CouponKafkaMessage.class);
            log.info("Receive CouponKafkaMessage:{}", message.toString());
            CouponStatus status = CouponStatus.of(couponInfo.getStatus());
            switch (status) {
                case USED:
                    break;
                case USABLE:
                    processUsedCoupons(couponInfo, status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo, status);
                    break;
            }
        }
    }

    /**
     * 处理已使用的用户优惠券
     *
     * @param couponInfo
     * @param status
     */
    private void processExpiredCoupons(CouponKafkaMessage couponInfo, CouponStatus status) {
        //TODO 给用户发送短信 不同于过期的的操作
        proceCouponByStatus(couponInfo, status);
    }

    /**
     * 处理已使用的用户优惠券
     *
     * @param couponInfo
     * @param status
     */
    private void processUsedCoupons(CouponKafkaMessage couponInfo, CouponStatus status) {
        //TODO 给用户发送短信 不同于已使用的操作
        proceCouponByStatus(couponInfo, status);
    }

    /**
     * <h2>根据状态清理优惠卷信息</h2>
     */
    private void proceCouponByStatus(CouponKafkaMessage couponInfo,
                                     CouponStatus status) {
        List<Coupon> coupons = couponDao.findAllById(couponInfo.getIds());
        if (CollectionUtils.isEmpty(coupons) ||
                coupons.size() != couponInfo.getIds().size()) {
            log.error("Can't not find right Coupon info:{}",
                    JSON.toJSONString(couponInfo));
            //TODO 发送邮件
            return;
        }
        coupons.forEach(c -> c.setStatus(status));
        log.info("CouponKafkaMessage op Coupon Count:{}", couponDao.saveAll(coupons));
    }
}
