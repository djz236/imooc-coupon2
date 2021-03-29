package com.imooc.coupon.thymeleaf;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.GoodsType;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.constant.ProductLine;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.TemplateRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠卷模板详情
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThyTemplateInfo {
    // 列表展示
    /*用户id（不是创建优惠卷模板的用户，是当前的查看用户）*/
    private Long userId;
    /*自增主键*/
    private Integer id;
    /*优惠卷名称*/
    private String name;
    /*优惠卷描述*/
    private String desc;
    /*优惠卷分类*/
    private String category;
    /*产品线*/
    private String productLine;
    // 详情展示
    /*过期规则描述*/
    private String expiration;
    /*折扣规则描述*/
    private String discount;
    /*使用条件描述*/
    private String usage;

    static ThyTemplateInfo to(CouponTemplateSDK templateSDK){
        ThyTemplateInfo info = new ThyTemplateInfo();
        info.setId(templateSDK.getId());
        info.setName(templateSDK.getName());
        info.setDesc(templateSDK.getDesc());
        info.setCategory(CouponCategory.of(templateSDK.getCaetgory()).getDescription());
        info.setProductLine(ProductLine.of(templateSDK.getProductLine()).getDescription());
        info.setExpiration(buildExpiration(templateSDK.getRule().getExpiration()));
        info.setDiscount(buildDiscount(templateSDK.getRule().getDiscount()));
        info.setUsage(buildUsage(templateSDK.getRule().getUsage()));
        return info;
    }
    /**
     * <h2>过期规则描述</h2>
     * @param expiration
     * @return
     */
    private static String buildExpiration(TemplateRule.Expiration expiration){
        return PeriodType.of(expiration.getPeriod()).getDescription()
                + "，有效间隔："
                + expiration.getGap()
                + "，优惠卷模板过期日期："
                + new SimpleDateFormat("yyyy-MM-dd").format(new Date(expiration.getDeadline()));
    }

    /**
     *<h2>过期规则描述</h2>
     * @param discount
     * @return
     */
    private static String buildDiscount(TemplateRule.Discount discount){
        return "基准：" +discount.getBase() + "，"+ "额度"+discount.getQuota();
    }

    /**
     * <h2>使用条件描述</h2>
     * @param usage
     * @return
     */
    @SuppressWarnings("all")
    public static String buildUsage(TemplateRule.Usage usage){
        List<Integer> goodsTypeI = JSON.parseObject(usage.getGoodsType(),List.class);
        List<String> goodsTpes =goodsTypeI.stream()
                .map(g-> GoodsType.of(g))
                .map(g->g.getDescription())
                .collect(Collectors.toList());
        return "省份："+usage.getProvince()+"，城市："+usage.getCity()+"，允许的商品类型给："
                +goodsTpes.stream().collect(Collectors.joining(","));
    }
}
