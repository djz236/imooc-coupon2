package com.imooc.coupon.thymeleaf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 创建优惠券模板
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThyCreateTemplate {
    /*优惠卷名称*/
    private String name;
    /*优惠卷 logo*/
    private String logo;
    /*优惠卷描述*/
    private String desc;
    /*优惠卷分类*/
    private String category;
    /*产品线*/
    private Integer productLine;
    /*总数*/
    private Integer count;
    /*创建用户*/
    private Long userId;
    /*目标用户*/
    private Integer target;
    /*有效期规则*/
    private Integer period;
    /*有效间隔：只对变动型有效期有效*/
    private Integer gap;
    /*优惠卷模板的失效日期：两类规则都有效，2020-07-04*/
    private String deadline = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    /*额度(quota):20（满减）、85%（折扣）、10（立减）*/
    private Integer quota;
    /*基准（需要满多少）：199*/
    private Integer base;
    /*每个人最多领几张的限制*/
    private Integer limitation;
    /*省份*/
    private String province;
    /*城市*/
    private String city;
    /*商品类型、list[文娱(1)、生鲜(2)、家居(3)、其他(4)、全品类(0)]*/
    private List<Integer> goodsType = new ArrayList<>();
    /*权重(可以和哪些卷叠加使用，需要验证同一类的优惠卷一定不能叠加)：list[],优惠卷的唯一编码*/
    private String weight;
}
