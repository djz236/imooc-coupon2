package com.imooc.coupon.thymeleaf;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.*;
import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.vo.TemplateRequest;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 优惠券模板controller
 */
@Slf4j
@Controller
@RequestMapping("/template/thy")
public class ThyTemplateController {
    private final CouponTemplateDao templateDao;
    private final IBuildTemplateService templateService;

    public ThyTemplateController(CouponTemplateDao templateDao,
                                 IBuildTemplateService templateService) {
        this.templateDao = templateDao;
        this.templateService = templateService;
    }

    /**
     * 优惠券系统录入
     * 127.0.0.1：7001/coupon-template/template/thy/home
     *
     * @return
     */
    @GetMapping("/home")
    public String home() {
        log.info("view home.");
        return "home";
    }

    /**
     * 查看优惠券模板详情
     * 127.0.0.1：7001/coupon-template/template/thy/info/{id}
     *
     * @param id
     * @param map
     * @return
     */
    @GetMapping("/info/{id}")
    public String info(@PathVariable
                               Integer id, ModelMap map) {
        log.info("view template info.");
        Optional<CouponTemplate> template = templateDao.findById(id);
        if (template.isPresent()) {
            CouponTemplate couponTemplate = template.get();
            map.put("template",
                    ThyTemplateInfo.to(couponTemplate));
        }
        return "template_detail";
    }

    /**
     * 查看优惠券模板列表
     * 127.0.0.1：7001/coupon-template/template/thy/list
     *
     * @param map
     * @return
     */
    @GetMapping("/list")
    public String list(ModelMap map) {
        log.info("view template list.");
        List<CouponTemplate> couponTemplateList = templateDao.findAll();
        List<ThyTemplateInfo> thyTemplateInfos = couponTemplateList.stream()
                .map(ThyTemplateInfo::to)
                .collect(Collectors.toList());
        map.addAttribute("templates", thyTemplateInfos);
        return "template_list";
    }

    /**
     * 创建优惠券模板
     *127.0.0.1：7001/coupon-template/template/thy/create
     * @param map
     * @param session
     * @return
     */
    @GetMapping("/create")
    public String create(ModelMap map, HttpSession session) {
        log.info("view template from.");
        session.setAttribute("category", CouponCategory.values());
        session.setAttribute("productLine", ProductLine.values());
        session.setAttribute("target", DisTributeTarget.values());
        session.setAttribute("period", PeriodType.values());
        session.setAttribute("goodsType", GoodsType.values());
        map.addAttribute("template", new ThyCreateTemplate());
        map.addAttribute("action","create");
        return "template_form";
    }

    /**
     * 创建优惠券模板
     * 127.0.0.1：7001/coupon-template/template/thy/create
     * @return
     */
    @PostMapping("/create")
    public String create(@ModelAttribute ThyCreateTemplate template)throws Exception {
        log.info("create info.");
        log.info("{}", JSON.toJSONString(template));
        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                template.getPeriod(),
                template.getGap(),
                new SimpleDateFormat("yyyy-MM-dd").parse(template.getDeadline()).getTime()
        ));
        rule.setDiscount(new TemplateRule.Discount(template.getQuota(),template.getBase()));
        rule.setLimitation(template.getLimitation());
        rule.setUsage(new TemplateRule.Usage(template.getProvince(),template.getCity(),
                JSON.toJSONString(template.getGoodsType())));
        rule.setWeight(
                JSON.toJSONString(Stream.of(template.getWeight().split(",")).collect(Collectors.toList()))
        );
        TemplateRequest request = new TemplateRequest(
                template.getName(),template.getLogo(),template.getDesc()
                ,template.getCategory(),template.getProductLine(),template.getCount(),
                template.getUserId(),template.getTarget(),rule
        );
        CouponTemplate object = templateService.buildTemplate(request);
        log.info("create coupon template:{}",JSON.toJSONString(object));
        return "redirect:/template/thy/list";
    }
}
