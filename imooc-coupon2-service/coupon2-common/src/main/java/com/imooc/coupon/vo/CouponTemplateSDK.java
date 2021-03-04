package com.imooc.coupon.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>微服务之间用的优惠卷模板信息定义</h1> CouponTemplate對應數據庫 比較特殊
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateSDK {
    /*优惠卷模板主键*/
    private Integer id;
    /*优惠卷名称*/
    private String name;
    /*优惠卷logo*/
    private String logo;
    /*优惠卷描述*/
    private String desc;
    /*优惠卷分类*/
    private String caetgory;
    /*产品线*/
    private Integer productLine;
    /*优惠卷模板的编码*/
    private String key;
    /*目标用户*/
    private Integer target;
    /*优惠卷规则*/
    private TemplateRule rule;
}