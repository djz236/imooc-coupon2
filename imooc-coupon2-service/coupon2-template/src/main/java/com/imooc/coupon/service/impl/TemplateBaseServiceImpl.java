package com.imooc.coupon.service.impl;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.ITemplateBaseService;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 优惠券模板基础服务接口实现
 */
@Slf4j
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {
    private final CouponTemplateDao couponTemplateDao;

    @Autowired
    public TemplateBaseServiceImpl(CouponTemplateDao couponTemplateDao) {
        this.couponTemplateDao = couponTemplateDao;
    }

    /**
     * 根据优惠券模板id 获取优惠券模板信息
     *
     * @param id 模板id
     * @return
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        //从 Java 8 引入的一个很有趣的特性是 Optional  类。
        // Optional 类主要解决的问题是臭名昭著的空指针异常（NullPointerException） ——
        //本质上，这是一个包含有可选值的包装类，这意味着 Optional 类既可以含有对象也可以为空。
        Optional<CouponTemplate> template = couponTemplateDao.findById(id);
        if (!template.isPresent()) {
            throw new CouponException("Template is not exist:"+id);
        }
        return template.get();
    }

    /**
     * 查找所有可用的优惠券模板
     * @return
     */
    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        List<CouponTemplate> templates = couponTemplateDao.findAllByAvailableAndExpired(true, false);
        //JDK8中有双冒号的用法，就是把方法当做参数传到stream内部，使stream的每个元素都传入到该方法里面执行一下。
        // 1. 不需要参数,返回值为 5          () -> 5
        // 2. 接收一个参数(数字类型),返回其2倍的值         x -> 2 * x
        // 3. 接受2个参数(数字),并返回他们的差值          (x, y) -> x – y
        // 4. 接收2个int型整数,返回他们的和          (int x, int y) -> x + y
        // 5. 接受一个 string 对象,并在控制台打印,不返回任何值(看起来像是返回void)      (String s) -> System.out.print(s)
        List<CouponTemplateSDK> collect =
                templates.stream().map(this::template2TemplateSDK).collect(Collectors.toList());
        return collect;
    }

    /**
     *
     * 获取模板ids 到CouponTemplateSDK 的映射
     *
     * @param ids 模板ids
     * @return Map<key:模板id,value:CouponTemplateSDK></key:模板id,value:CouponTemplateSDK>
     */
    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSdk(Collection<Integer> ids) {
        List<CouponTemplate> templates =
                couponTemplateDao.findAllById(ids);

        return templates.stream()
                .map(this::template2TemplateSDK)
                .collect(Collectors.toMap(
                        CouponTemplateSDK::getId,
                        Function.identity()
                ));
    }

    /**
     *
     * CouponTemplate 转为 CouponTemplateSDK
     *
     * @param template
     * @return
     */
    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template){
        return new CouponTemplateSDK(
          template.getId(),
          template.getName(),
          template.getLogo(),
          template.getDesc(),
          template.getCategory().getCode(),
          template.getProductLine().getCode(),
          template.getKey(),
          template.getTarget().getCode(),
          template.getRule()
        );
    }
}
