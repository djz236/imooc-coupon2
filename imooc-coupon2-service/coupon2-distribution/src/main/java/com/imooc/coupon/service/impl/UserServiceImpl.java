package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.service.IRedisService;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务相关的接口实现
 * 所有的操作过程，状态都保存在redis中，并通过 Kafka 把消息传递到 MySql 中
 * 为什么使用 Kafka，而不是直接使用Springboot 中的异步处理？ 失败后kafka 可以有好的解决方案 高可用
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    /*coupon Dao 接口*/
    private final CouponDao couponDao;
    /*Redis 服务*/
    private final IRedisService redisService;
    /*模板微服务客户端*/
    private final TemplateClient templateClient;
    /*结算微服务客户端*/
    private final SettlementClient settlementClient;
    /*Kafka 客户端*/
    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserServiceImpl(CouponDao couponDao, IRedisService redisService, TemplateClient templateClient,
                           SettlementClient settlementClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 根据用户id和状态查询优惠卷记录
     *
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return
     * @throws CouponException
     */
    @Override
    public List<Coupon> findCouponByStatus(Long userId, Integer status) throws CouponException {
        List<Coupon> cachedCoupons = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;
        if (!CollectionUtils.isEmpty(cachedCoupons)) {
            log.debug("coupon cache is not empty:{},{}", userId, status);
            preTarget = cachedCoupons;
        } else {
            log.debug("coupon cache is empty, get coupon from db:{},{}",
                    userId, status);
            List<Coupon> dbCoupons = couponDao.findallByUserIdAndStatus(userId, CouponStatus.of(status));
            // 如果数据库中没有记录，直接返回就可以，Cache 中已经加入了一张无效的优惠卷
            if (CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("current user do not have coupon,{},{}", userId, status);
                return dbCoupons;
            }
            //填充 dbCoupons 的 templateSDK字段
            Map<Integer, CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK(dbCoupons.stream()
                    .map(Coupon::getTemplateId)
                    .collect(Collectors.toList())).getData();
            dbCoupons.forEach(dc -> {
                dc.setTemplateSDK(id2TemplateSDK.get(dc.getTemplateId()));
            });
            preTarget = dbCoupons;
            //将记录写入Cache
            redisService.addCouponToCache(userId, preTarget, status);
        }
        // 将无效优惠卷去除
        preTarget = preTarget.stream().filter(c -> c.getId() != -1)
                .collect(Collectors.toList());
        // 如果当前获取的是可用优惠卷，还需要做对已过期优惠卷的延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            //如果已经过期状态不为空，需要做延迟处理
            if (!CollectionUtils.isEmpty(classify.getExpired())) {
                log.info("Add Expired Coupon To Cache From FinfCouponsByStatus:{},{}", userId, status);
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());
                // 发送到 Kafka中做异步处理
                kafkaTemplate.send(Constant.TOPIC,
                        JSON.toJSONString(new CouponKafkaMessage(
                                CouponStatus.EXPIRED.getCode(),
                                classify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList())
                        )));
            }
            return classify.getUsable();

        }
        return preTarget;
    }

    /**
     * 根据用户id查到当前可以领取的优惠卷模板
     *
     * @param userId
     * @return
     * @throws CouponException
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = new Date().getTime();
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();
        log.info("Find All Template(From TemplateClient) Count:{}",
                templateSDKS.size());
        // 过滤过期的优惠卷模板
        templateSDKS = templateSDKS.stream().filter(
                c -> c.getRule().getExpiration().getDeadline() > curTime
        ).collect(Collectors.toList());
        log.info("Find Usable Template Count:{}", templateSDKS.size());
        //key 是 templateId
        //value 中的 left 是 Template Limitation领取的次数上限，right 是优惠卷模板
        //当一个函数返回两个值并且两个值都有重要意义时我们一般会用Map的key和value来表达，
        // 但是这样的话就需要两个键值对，用Map映射去做处理时，
        // 此时的key相当于value的一个描述或者引用，而具体的信息都保存在value中，
        // 我们可以通过key去获取对应的value。但是当key和value都保存具体信息时，
        // 我们就需要用到Pair对了。Pair对也是键值对的形式。
        /*Pair<String, String> pair = Pair.of("aku", "female");
        pair.getLeft();
        pair.getRight();
        这种Pair的返回对一个函数返回两个都有意义的值有特别用处。*/
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());
        //templateSDKS 值转到 limit2Template
        templateSDKS.forEach(t -> limit2Template.put(
                t.getId(),
                Pair.of(t.getRule().getLimitation(), t)
                )
        );

        //根据用户id查到当前可以领取的优惠卷模板
        List<CouponTemplateSDK> result = new ArrayList<>();
        List<Coupon> userUsableCoupons = findCouponByStatus(userId, CouponStatus.USABLE.getCode());
        log.debug("Current User Has Usable Coupons:{},{}", userId, userUsableCoupons.size());
        //key 是 templateId   Collectors.groupingBy 根据一个或多个属性对集合中的项目进行分组
        //
        // {
        //  "啤酒":[
        //          {"category":"啤酒","id":4,"name":"青岛啤酒","num":3,"price":10},
        //          {"category":"啤酒","id":5,"name":"百威啤酒","num":10,"price":15}
        //         ],
        //  "零食":[
        //          {"category":"零食","id":1,"name":"面包","num":1,"price":15.5},
        //          {"category":"零食","id":2,"name":"饼干","num":2,"price":20},
        //          {"category":"零食","id":3,"name":"月饼","num":3,"price":30}
        //         ]
        //  }

        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        // 根据 Template 的 Rule 判断是否可以领取 优惠卷模板
        limit2Template.forEach((k, v) -> {
            Integer limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();
            if (templateId2Coupons.containsKey(k)
                    && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    /**
     * <h2>用户领取优惠卷</h2>
     * 1.从TemplateClient 中拿到对应的优惠卷 ，并检查是否过期
     * 2.根据limitation 判断用户是否可以领取
     * 3.save to db
     * 4.填充 CouponTemplateSDK
     * 5.save to cache
     *
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     * @throws CouponException
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        Map<Integer, CouponTemplateSDK> ids2TemplateSDK = templateClient.findIds2TemplateSDK(
                Collections.singletonList(request.getTemplateSDK().getId())
        ).getData();
        // // 优惠卷模板是需要存在的
        if (ids2TemplateSDK.size() <= 0) {
            log.error("Can Not Aquire Template From TemplateClient:{}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Aquire Template From TemplateClient");
        }

        // 用户是否可以领取这张优惠卷
        List<Coupon> userUsableCoupons = findCouponByStatus(request.getUserId(),
                CouponStatus.USABLE.getCode());
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream().collect(
                Collectors.groupingBy(Coupon::getTemplateId));
        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())
                && templateId2Coupons.get(request.getTemplateSDK().getId()).size() >= request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Exceed Template Assign Limitation:{}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }
        //尝试去获取优惠卷码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Can Not Aquire Coupon Code:{}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }
        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(), request.getUserId(),
                couponCode, CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);
        //填充 Coupon 对象的 CouponTemplateSDK,一定要在放入缓存之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());
        // 放入缓存中
        redisService.addCouponToCache(
                request.getUserId()
                , Collections.singletonList(newCoupon)
                , CouponStatus.USABLE.getCode()
        );
        return newCoupon;
    }

    /**
     * <h2>结算（核销） 优惠卷</h2>
     * 这里需要注意，规则相关处理需要由 Settlement 系统去做，当前系统仅仅做业务处理过程（校验过程）
     *
     * @param info
     * @return
     * @throws CouponException
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        // 当没有传递优惠卷时，直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();

        if (CollectionUtils.isEmpty(ctInfos)) {
            log.info("Empty Coupons For Settle.");
            double goodSum = 0.0;
            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodSum += gi.getPrice() * gi.getCount();
            }
            // 没有优惠卷 也就不存在优惠卷的核销，SettlementInfo  其他字段不需要修改
            info.setCost(retain2Decimals(goodSum));
        }
        // 校验传递的优惠卷是否是用户自己的
        List<Coupon> coupons = findCouponByStatus(info.getUserId(), CouponStatus.USABLE.getCode());
        //    private static void identity() {
        //        Stream<String> stream = Stream.of("I", "love", "you", "too");
        //        Map<String, Integer> map = stream.collect(Collectors.toMap(Function.identity(), String::length));
        //        System.out.println(map);
        //    }
        //        {love=4, too=3, I=1, you=3}
        //Function.identity()返回一个输出跟输入一样的Lambda表达式对象，等价于形如t -> t形式的Lambda表达式
        Map<Integer,Coupon> id2Coupon=coupons.stream()
                .collect(Collectors.toMap(Coupon::getId,
                        Function.identity()));
        if(id2Coupon.isEmpty()||
                org.apache.commons.
                        collections.CollectionUtils.
                        isSubCollection(ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()),
                                id2Coupon.keySet())
        ){
            log.info("{}",id2Coupon.keySet());
            log.info("{}", ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()));
            log.error("User Coupon Has Some Problem,It Is Not SubCollection Of Coupons!");
            throw  new CouponException("User Coupon Has Some Problem,It Is Not SubCollection Of Coupons!");
        }
        log.debug("Current Settlemen Coupons Is User's:{}",ctInfos.size());
        List<Coupon> settleCoupons =  new ArrayList<>(ctInfos.size());

        ctInfos.forEach(ci->settleCoupons.add(id2Coupon.get(ci.getId())));
        // 通过结算服务，获取结算信息
        SettlementInfo processedInfo = settlementClient.computeRule(info).getData();
        //employ 是否使结算生效，即核销
        if(processedInfo.getEmploy()
        &&!CollectionUtils.isEmpty(
                processedInfo.getCouponAndTemplateInfos()
        )){
            log.info("Settle User Coupon:{},{}",info.getUserId(),
                    JSON.toJSONString(settleCoupons));
            //更新缓存
            redisService.addCouponToCache(
                    info.getUserId(),
                    settleCoupons,
                    CouponStatus.USED.getCode()
            );
            // 更新db
            kafkaTemplate.send(
                    Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(),
                            settleCoupons.stream().map(Coupon::getId)
                            .collect(Collectors.toList())
                    ))
            );
        }
        return processedInfo;
    }


    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    private double retain2Decimals(Double value) {
        // BigDecimal.ROUND_HALF_UP 四舍五入  2.125456 = 2.13
        return new BigDecimal(value)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
