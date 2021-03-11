package com.imooc.coupon.service.impl;

import com.imooc.coupon.service.IKafkaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaServiceImpl implements IKafkaService {
    @Override
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {

    }
}
