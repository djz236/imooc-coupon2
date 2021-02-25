package com.imooc.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import javax.jnlp.FileContents;
import javax.servlet.Filter;
import java.io.FileFilter;

public abstract class AbstractPostZuulFilter extends AbstractZuulFilter{
    /**
     * filterType代表过滤类型
     * PRE: 该类型的filters在Request routing到源web-service之前执行。用来实现Authentication、选择源服务地址等
     * ROUTING：该类型的filters用于把Request routing到源web-service，源web-service是实现业务逻辑的服务。这里使用HttpClient请求web-service。
     * POST：该类型的filters在ROUTING返回Response后执行。用来实现对Response结果进行修改，收集统计数据以及把Response传输会客户端。
     * ERROR：上面三个过程中任何一个出现错误都交由ERROR类型的filters进行处理。
     * 主要关注 pre、post和error。分别代表前置过滤，后置过滤和异常过滤。
     * 如果你的filter是pre的，像上一篇那种，就是指请求先进入pre的filter类，你可以进行一些权限认证，日志记录，或者额外给Request增加一些属性供后续的filter使用。pre会优先按照order从小到大执行，然后再去执行请求转发到业务服务。
     * 再说post，如果type为post，那么就会执行完被路由的业务服务后，再进入post的filter，在post的filter里，一般做一些日志记录，或者额外增加response属性什么的。
     * 最后error，如果在上面的任何一个地方出现了异常，就会进入到type为error的filter中。
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
