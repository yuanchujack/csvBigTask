package com.yc.data.config;

import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>
 * MybatisPlusConfig
 * </p>
 *
 * @author cy
 * @since 2023-02-10 21:22:05
 * @description 由 Mybatisplus Code Generator 创建
 **/
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {

    /**
     *  sql性能分析插件，输出sql语句及所需时间
     * 注意：mybatis plus 3.1.2以上版本不支持该插件，请使用p6spy
     * @return
     */
    @Bean
    @Profile({"dev","test","sit","uat"})
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        /** SQL 执行性能分析，开发环境使用，线上不推荐。 maxTime 指的是 sql 最大执行时长 */
        performanceInterceptor.setMaxTime(5000);
        /** SQL是否格式化 默认false */
        performanceInterceptor.setFormat(true);
        return performanceInterceptor;
    }

}
