package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 路径创建请求对象定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePathRequest {
    private List<PathInfo> pathInfo;

    /**
     * Builder 使用创建者模式又叫建造者模式。简单来说，就是一步步创建一个对象，
     * 它对用户屏蔽了里面构建的细节，但却可以精细地控制对象的构造过程。
     *
     * @Builder注释为你的类生成相对略微复杂的构建器API。@Builder可以让你以下面显示的那样调用你的代码，来初始化你的实例对象： Student.builder()
     * .sno( "001" )
     * .sname( "admin" )
     * .sage( 18 )
     * .sphone( "110" )
     * .build();
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathInfo {
        /*路径模式*/
        private String pathPattern;
        /*HTTP 请求方法*/
        private String httpMethod;
        /**
         * 路径名称
         */
        private String pathName;
        /**
         * 服务名称
         */
        private String serviceName;
        /**
         * 操作模式：READ,WRITE
         */
        private String opMode;
    }

}
