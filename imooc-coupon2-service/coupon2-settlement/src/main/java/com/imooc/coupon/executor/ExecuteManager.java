package com.imooc.coupon.executor;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 优惠卷结算规则执行管理器
 * 即根据用户的请求（SettlementInfo）找到对应的Executor，去做结算
 * BeanPostProcessor: Bean 后置处理器 当所有的bean被spring创建之后
 */
@Slf4j
@Component
public class ExecuteManager implements BeanPostProcessor {
    /**
     * 规则执行器映射
     */
    private static Map<RuleFlag, RuleExecutor> executorIndex =
            new HashMap<>(RuleFlag.values().length);

    /**
     * 优惠券结算规则入口
     * 注意：一定要保证传递进来的优惠券个数
     */
    public SettlementInfo computeRule(SettlementInfo settlementInfo)
            throws CouponException {
        SettlementInfo result = null;
        //单类优惠券
        if (settlementInfo.getCouponAndTemplateInfos().size() == 1) {
            //获取优惠券类别
            CouponCategory couponCategory = CouponCategory.of(
                    settlementInfo.getCouponAndTemplateInfos()
                            .get(0)
                            .getTemplateSDK()
                            .getCaetgory()
            );
            switch (couponCategory) {
                case LIJIAN:
                    result =
                            executorIndex.get(RuleFlag.MANJIAN).computeRule(settlementInfo);
                    break;
                case ZHEKOU:
                    result =
                            executorIndex.get(RuleFlag.ZHEKOU).computeRule(settlementInfo);
                    break;
                case MANJIAN:
                    result =
                            executorIndex.get(RuleFlag.LIJIAN).computeRule(settlementInfo);
                    break;
            }
        } else {
            //多累优惠券
            ArrayList<Object> category =
                    new ArrayList<>(settlementInfo.getCouponAndTemplateInfos().size());
            settlementInfo.getCouponAndTemplateInfos().forEach(
                    ct -> {
                        category.add(CouponCategory.of(ct.getTemplateSDK().getCaetgory()));
                    }
            );
            if (category.size() != 2) {
                throw new CouponException("Not Suppor For More Template " +
                        "Category");
            } else {
                if (category.contains(CouponCategory.MANJIAN)
                        && category.contains(CouponCategory.ZHEKOU)) {
                    result =
                            executorIndex.get(RuleFlag.MANJIAN_ZHEKOU).computeRule(settlementInfo);
                } else {
                    throw new CouponException("Not Support For Other Template" +
                            " Category");
                }
            }
        }
        return result;
    }

    /**
     * 在bean 初始化之前去执行（before）
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean,
                                                  String beanName)
            throws BeansException {
        if (!(bean instanceof RuleExecutor)) {
            return bean;
        }
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();
        if (executorIndex.containsKey(ruleFlag)) {
            throw new IllegalStateException("There Is already an executor for" +
                    " rule flag:" + ruleFlag);
        }
        log.info("Load Executor {} For Rule Flag{}.", executor, getClass(),
                ruleFlag);
        executorIndex.put(ruleFlag,executor);
        return null;
    }

    /**
     * 在bean 初始化 之后去执行（after）
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean,
                                                 String beanName) throws BeansException {
        return bean;
    }
}
