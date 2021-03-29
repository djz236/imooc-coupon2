package com.imooc.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则执行器抽象类，   定义通用方法
 */
public abstract class AbstractExecutor {
    /**
     * <h2>校验商品类型与优惠卷是否匹配</h2>
     * 需要注意：
     * 1.这里实现的单品类优惠卷的校验，多品类优惠卷重载此方法
     * 2.商品只需要有一个优惠卷要求的商品类型 去匹配就可以
     *
     * @param settlement
     * @return
     */
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType)
                .collect(Collectors.toList());
        List<Integer> list = JSON.parseObject(
                settlement.getCouponAndTemplateInfos()
                        .get(0)
                        .getTemplateSDK()
                        .getRule()
                        .getUsage()
                        .getGoodsType(),
                List.class
        );
        //存在交集即可
        return !CollectionUtils.isEmpty(
                org.apache.commons.collections.CollectionUtils.intersection(goodsType, list)
        );
    }

    /**
     * <h2>处理商品类型与优惠卷限制不匹配的情况</h2>
     *
     * @param settlementInfo {@link SettlementInfo} 用户传递的结算信息
     * @param goodsSum       商品总价
     * @return {@link SettlementInfo} 已经修改过的结算信息
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlementInfo,
                                                        Double goodsSum) {
        boolean isgoodsTypeSatisfy = isGoodsTypeSatisfy(settlementInfo);
        //当商品类型不满足时，直接返回总价，并清空优惠券
        if(!isgoodsTypeSatisfy){
            settlementInfo.setCost(goodsSum);
            settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
            return settlementInfo;
        }
        return null;
    }

    /**
     * 商品总价
     *
     * @param goodsInfos
     * @return
     */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos) {
        return goodsInfos.stream().mapToDouble(
                g -> g.getCount() * g.getPrice()
        ).sum();
    }

    /**
     * 保留两位小数
     */
    protected double return2Decimals(double value) {
        return new BigDecimal(value).setScale(
                2, BigDecimal.ROUND_HALF_UP
        ).doubleValue();
    }

    /**
     * 支付最小价格
     */
    protected double minCost() {
        return 0.1;
    }
}
