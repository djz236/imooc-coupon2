package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.DisTributeTarget;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.constant.ProductLine;
import com.imooc.coupon.vo.TemplateRequest;
import com.imooc.coupon.vo.TemplateRule;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {
    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void testBuildTemplate() throws Exception {
        TemplateRequest templateRequest = fakeTemplateRequest();
        System.out.print(
                JSON.toJSONString(buildTemplateService.buildTemplate(templateRequest))
        );
        //防止主线程执行结束后，创建优惠券码的子线程结束导致报错失败。
        Thread.sleep(5000);
    }

    /**
     * <h2>fake TemplateRequest</h2>
     *
     * @return
     */
    private TemplateRequest fakeTemplateRequest() {
        TemplateRequest re = new TemplateRequest();
        re.setName("优惠券模板-" + new Date().getTime());
        re.setLogo("www.baidu.com");
        re.setDesc("这是一张优惠券模板");
        String code = CouponCategory.MANJIAN.getCode();
        re.setCategory(code);
        re.setProductLine(ProductLine.DAMAO.getCode());
        re.setCount(10);
        re.setUserId(1008L);
        re.setTarget(DisTributeTarget.SINGLE.getCode());
        TemplateRule rule = new TemplateRule();
        //天: 加、减操作
        //  System.out.println("昨天的这个时候：" + sd.format(DateUtils.addDays(now, -1)));
        //System.out.println("明天的这个时候：" + sd.format(DateUtils.addDays(now, 1)));
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(), 1, DateUtils.addDays(new Date(),
                60).getTime()
        ));
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage(
                "河北省", "石家庄市",
                JSON.toJSONString(Arrays.asList("文娱", "家具"))
        ));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));
        re.setRule(rule);
        return re;
    }
}
