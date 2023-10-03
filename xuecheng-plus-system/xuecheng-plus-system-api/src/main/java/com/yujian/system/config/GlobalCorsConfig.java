package com.yujian.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 允许任何域名使用该域名,*代表允许任意域名跨域访问
        corsConfiguration.addAllowedHeader("*"); // 允许任何头
        corsConfiguration.addAllowedMethod("*"); // 允许任何方法
        corsConfiguration.setAllowCredentials(true); // 允许cookies跨域

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //第一个参数:哪些路径走该过滤器,第二个参数:过滤器配置
        source.registerCorsConfiguration("/**",corsConfiguration);
        CorsFilter corsFilter = new CorsFilter(source);
        return corsFilter;
    }
}
