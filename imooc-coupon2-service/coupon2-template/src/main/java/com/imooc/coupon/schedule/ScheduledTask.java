package com.imooc.coupon.schedule;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时清理已经过期的优惠券模板
 */
@Slf4j
@Component
public class ScheduledTask {
    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduledTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    /**
     * 下线已过期的优惠券模板
     * fixedRate 任务两次执行时间间隔是任务的开始点，
     * 而 fixedDelay 的间隔是前次任务的结束与下次任务的开始
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlinCouponTemplate() {
        log.info("Start To Expire CouponTemplate.");
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)) {
            log.info("Done To Expired CouponTemplate.");
            return;
        }
        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        templates.stream()
                .forEach(t -> {
                    // 根据优惠卷模板中规则中的 “过期规则” 校验模板是否过期
                    TemplateRule rule = t.getRule();
                    if (rule.getExpiration().getDeadline() < cur.getTime()) {
                        t.setExpired(true);
                        expiredTemplates.add(t);
            }
        });
        if(!CollectionUtils.isEmpty(expiredTemplates)){
            List<CouponTemplate> o = templateDao.saveAll(expiredTemplates);
            log.info("Expired CouponTemplate num:{}",o);
        }
        log.info("Done To Expired CouponTemplate.");
    }
}
