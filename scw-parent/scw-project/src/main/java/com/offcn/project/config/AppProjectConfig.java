package com.offcn.project.config;

import com.offcn.utils.OSSTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppProjectConfig {
    @ConfigurationProperties(prefix="oss") //加载配置文件中的oss属性
    @Bean
    public OSSTemplate ossTemplate(){
        return new OSSTemplate();
    }
}
