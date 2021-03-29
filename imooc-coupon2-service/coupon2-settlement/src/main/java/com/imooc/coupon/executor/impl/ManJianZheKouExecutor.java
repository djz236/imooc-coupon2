package com.imooc.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>满减 + 折扣优惠卷折扣规则执行器</h1>
 * @Author: crowsjian
 * @Date: 2020/6/25 10:11
 */
@Slf4j
@Component
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * <h2>规则类型标记</h2>
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    /**
     * <h2>校验商品类型与优惠卷是否匹配</h2>
     * 需要注意：
     * 1.这里实现的满减 + 折扣品类优惠卷的校验，多品类优惠卷重载此方法
     * 2.如果想要使用多类优惠卷，则必须要所有的商品类型都包含在内
     * @param settlement {@link SettlementInfo} 用户传递的结算信息
     * @return
     */
    @Override
    @SuppressWarnings("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        log.debug("Check ManJian And ZheKou Is Match Or Not!");
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();
        settlement.getCouponAndTemplateInfos().forEach(ct->{
            templateGoodsType.addAll(
                    JSON.parseObject(
                            ct.getTemplateSDK().getRule().getUsage().getGoodsType(),
                            List.class
                    )
            );
        });
        // 如果想要使用多类优惠卷，则必须要所有的商品类型都包含在内，即差集为空
        return CollectionUtils.isEmpty(org.apache.commons.collections.CollectionUtils.subtract(
                goodsType,templateGoodsType
        ));
    }

    /**
     * <h2>优惠卷规则的计算</h2>
     * @param settlement {@link SettlementInfo} 包含了选择的优惠卷
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = return2Decimals(
                goodsCostSum(settlement.getGoodsInfos())
        );
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement,goodsSum);
        if(null!=probability){
            log.debug("ManJian And ZheKou Template Is Not Match To GoodsType!");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo manjian = null;
        SettlementInfo.CouponAndTemplateInfo zhekou = null;
        for(SettlementInfo.CouponAndTemplateInfo ct:settlement.getCouponAndTemplateInfos()){
            if(CouponCategory.of(ct.getTemplateSDK().getCaetgory())==CouponCategory.MANJIAN){
                manjian = ct;
            }else{
                zhekou = ct;
            }
        }
        assert null != manjian;
        assert null != zhekou;
        //当前的折扣优惠卷和满减卷如果不能共用（一起使用），清空优惠卷，返回商品原价
        if(!isTemplateCanShared(manjian,zhekou)){
            log.debug("Current Manjian And Zhekou Can Not Shared!");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double manjianBase = (double)manjian.getTemplateSDK().getRule()
                .getDiscount().getBase();
        double manjianQuota = (double)manjian.getTemplateSDK().getRule()
                .getDiscount().getQuota();
        // 最终价格
        // 先计算满减
        double targetSum = goodsSum;
        if(goodsSum >= manjianBase){
            targetSum -= manjianQuota;
            ctInfos.add(manjian);
        }
        // 再计算折扣
        double zhekouQuota = (double)zhekou.getTemplateSDK().getRule()
                .getDiscount().getQuota();
        targetSum *= zhekouQuota*1.0/100;
        ctInfos.add(zhekou);
        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(
                return2Decimals(
                        targetSum>minCost()?targetSum:minCost()
                )
        );
        log.debug("Use ManJian And ZheKou Coupon Make Goods Cost From {} to {}",
                goodsSum,settlement.getCost());
        return settlement;
    }

    /**
     * <h2>校验当前的两张优惠卷是否可以共用</h2>
     * 即校验 TemplateRule 中的 weight 是否满足操作
     * @param manjian
     * @param zhekou
     * @return
     */
    @SuppressWarnings("all")
    private boolean isTemplateCanShared(
            SettlementInfo.CouponAndTemplateInfo manjian,
            SettlementInfo.CouponAndTemplateInfo zhekou
    ){
        String manjianKey = manjian.getTemplateSDK().getKey()
                + String.format("%04d",manjian.getTemplateSDK().getId());
        String zhekouKey = manjian.getTemplateSDK().getKey()
                + String.format("%04d",zhekou.getTemplateSDK().getId());
        List<String> allSharedKeysForManjian = new ArrayList<>();
        allSharedKeysForManjian.add(manjianKey);//把自己添加
        allSharedKeysForManjian.addAll(JSON.parseObject(
                manjian.getTemplateSDK().getRule().getWeight(),
                List.class
        ));//可以和其他的那些优惠券可以一起使用呢 包含权重中设置的那些
        List<String> allSharedKeysForZhekou = new ArrayList<>();
        allSharedKeysForZhekou.add(zhekouKey);
        allSharedKeysForZhekou.addAll(JSON.parseObject(
                zhekou.getTemplateSDK().getRule().getWeight(),
                List.class
        ));
        return org.apache.commons.collections.CollectionUtils.isSubCollection(
                Arrays.asList(manjianKey,zhekouKey),allSharedKeysForManjian
        )|| org.apache.commons.collections.CollectionUtils.isSubCollection(
                Arrays.asList(manjianKey,zhekouKey),allSharedKeysForZhekou
        );
    }
}
