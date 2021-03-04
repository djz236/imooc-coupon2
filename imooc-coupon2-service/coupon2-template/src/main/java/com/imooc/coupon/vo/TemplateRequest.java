package com.imooc.coupon.vo;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.DisTributeTarget;
import com.imooc.coupon.constant.ProductLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    /**
     * 优惠券名称
     */
    private String name;
    /*优惠卷logo*/
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
    /*优惠卷规则*/
    private TemplateRule rule;

    /**
     * 校核对象的合法性
     *
     * @return
     */
    public boolean validate() {
        boolean stringValid = StringUtils.isNotBlank(name)
                && StringUtils.isNotBlank(logo)
                && StringUtils.isNotBlank(desc);
        boolean enumValid = null != CouponCategory.of(category)
                && null != ProductLine.of(productLine)
                && null != DisTributeTarget.of(target);
        boolean numValid = count > 0 && target > 0;
        return stringValid && enumValid && numValid && rule.validate();
    }
}
