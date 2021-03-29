package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {
    /**
     * redis客户端
     */
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get coupon from cache:{},{}", userId, status);
        String redisKey = status2RedisKey(status, userId);
        List<String> collect = redisTemplate.opsForHash()
                .values(redisKey)//获取指定变量中的hashMap值。
                .stream()//stream()优点无存储。
                // stream不是一种数据结构，它只是某种数据源的一个视图，数据源可以是一个数组，Java容器或I/O channel等。
                .map(o -> Objects.toString(o, null))//map方法类似一个迭代器，
                // 对调用这个Stream.map(**)的对象进行lambda表达式操作,您可以将对象转换为其他对象
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return collect.stream()
                .map(cs -> JSON.parseObject(cs, Coupon.class))
                .collect(Collectors.toList());
    }

    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save Empty List to cache for user :{} status:{}",
                userId, status);
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));
        //使用 SessionCallBack 把数据命令当如到redis的pieline
        //sessioncallback接口，通过这个接口就可以把多个命令放入到同一个Redis连接中去执行，可以使用同一个连接进行批量执行
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                status.forEach(s -> {
                    String redisKey = status2RedisKey(s, userId);
                    redisOperations.opsForHash()
                            .putAll(redisKey, invalidCouponMap);//以map集合的形式添加键值对。
                });
                return null;
            }
        };
        //使用Pipeline可以批量执行redis命令，防止多个命令建立多个连接
        List<Object> executePipelined = redisTemplate.executePipelined(sessionCallback);
        log.info("Pipeline Exe Result:{}", JSON.toJSONString(executePipelined));
    }

    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());
        // 因为优惠卷码不存在顺序关系，左边pop或右边pop,没有影响
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire Coupon code:{},{},{}", templateId, redisKey, couponCode);
        return couponCode;
    }

    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        log.info("Add Coupon To Cache:{},{},{}",
                userId, JSON.toJSONString(coupons), status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                result = addCouponToCacheForUsable(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }
        return result;
    }

    /**
     * 将过期优惠卷加入到 Cache 中
     *
     * @param userId
     * @param coupons
     * @return
     */
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons)
            throws CouponException {
        // status 是 EXPIRED, 代表是已有的优惠卷过期了，影响到两个 Cache
        // USABLE, EXPIRED
        log.debug("Add Coupon To Cache  For Expired.");
        //最终需要保存的 Cache
        Map<String, String> needCacheForExpired = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        String redisKeyForExpired = status2RedisKey(
                CouponStatus.EXPIRED.getCode(), userId
        );
        List<Coupon> curUsableCoupons = getCachedCoupons(
                userId, CouponStatus.USABLE.getCode()
        );
        //当前可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > 1;
        coupons.forEach(c -> needCacheForExpired.put(c.getId().toString(), JSON.toJSONString(c)));
        //校验当前优惠券参数是否与Cache中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramId = coupons.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
        if (!org.apache.commons.collections.CollectionUtils.isSubCollection(paramId, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache: {}, {}, {}",
                    userId, JSON.toJSONString(curUsableIds),
                    JSON.toJSONString(paramId));
            throw new CouponException("CurCoupon Is Not Equal To Cache");
        }
        List<String> needCleanKey = paramId.stream()
                .map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //1.已过期的优惠券Cache缓存
                redisOperations.opsForHash().putAll(
                        redisKeyForExpired,
                        needCacheForExpired
                );
                //2.可用优惠券Cache需要清理
                redisOperations.opsForHash().delete(
                        redisKeyForUsable,
                        needCleanKey.toArray()
                );
                //3.重置过期时间
                redisOperations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );

                redisOperations.expire(
                        redisKeyForExpired,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );

                return null;
            }
        };
        log.info("Pipeline Exe Result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * 将已使用的优惠券加入到Cache中
     *
     * @param userId
     * @param coupons
     * @return
     */
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons) throws CouponException {
        // 如果 status 是USED，代表用户操作是使用当前的优惠券，影响到两个Cache
        // USABLE可用，USED使用
        log.debug("Add Coupon To Cache  For Expired.");
        Map<String, String> needCachedForUsed = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);
        //获取缓存中的可用的优惠券 用户id
        List<Coupon> curUsableCoupons = getCachedCoupons(userId,
                CouponStatus.USABLE.getCode());
        //当前可用的优惠券一定是大于1的
        assert curUsableCoupons.size() > 1;
        coupons.forEach(c -> needCachedForUsed.put(
                c.getId().toString(),
                JSON.toJSONString(c)
        ));
        // 校验当前的优惠卷参数是否与 Cache 中相匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        //操作的coupon的id
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        // isSubCollection   list1是否list2集合子集
        if (!org.apache.commons.collections.CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("Coupon is not equal to cache:{},{},{}",
                    userId, JSON.toJSONString(curUsableCoupons),
                    JSON.toJSONString(paramIds));
            throw new CouponException("Coupon is not equal to cache.");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 1.已过期的 Cache 缓存
                redisOperations.opsForHash().putAll(
                        redisKeyForUsed,
                        needCachedForUsed
                );
                //2.可用的优惠券Cache需要清理  删除变量中的键值对，可以传入多个参数，删除多个键值对。
                redisOperations.opsForHash().delete(
                        redisKeyForUsable,
                        needCleanKey.toArray()
                );
                //3、重置过期时间
                redisOperations.expire(
                        redisKeyForUsed,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                redisOperations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                return null;
            }
        };
        log.info("",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * <h2>新增加优惠卷到 cache 中</h2>
     *
     * @param userId
     * @param coupons
     * @return
     */
    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons) {
        //如果status 是 USABLE ,代表是新增加的优惠券
        //只会影响一个 cache:USER_COUPON_USABLE
        log.debug("Add Coupon To Cache For Usable.");
        Map<String, String> needCacheObject = new HashMap<>();
        coupons.forEach(
                c -> needCacheObject.put(
                        c.getId().toString(), JSON.toJSONString(c)
                ));
        String rediskey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        //以map集合的形式添加键值对
        redisTemplate.opsForHash().putAll(rediskey, needCacheObject);
        log.info("add {} Coupons to cache:{},{}",
                needCacheObject.size(), userId, rediskey);
        //有时候希望给添加的缓存设置生命时间，到期后自动删除该缓存。可以使用 key是键，需要是已经存在的，
        // seconds是时间，单位是long类型，最后一个参数单位。
        /* 因为Redis要使用内存，但内存不是无限大。
        数据设置失效时间后，到期会自动删除数据，以释放空间，腾空。。。*/
        redisTemplate.expire(rediskey, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
        return 1;
    }

    private String status2RedisKey(Integer status, Long userId) {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;
    }

    /**
     * <h2>获取一个随机的过期时间</h2>
     * 缓存雪崩：key 在同一时间失效
     *
     * @param min 最小的小时数
     * @param max 做大的小时数
     * @return 返回[min, max] 之间的随机秒数
     */
    private Long getRandomExpirationTime(Integer min, Integer max) {
        return RandomUtils.nextLong(min * 60 * 60, max * 60 * 60);
    }

}
