package com.nwu.medimagebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * <p>
 * 提供RestTemplate实例，用于调用Python后端API
 * </p>
 * 
 * @author MedImage团队
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate实例
     * 
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(getClientHttpRequestFactory());
    }
    
    /**
     * 创建ClientHttpRequestFactory实例
     * <p>
     * 配置连接超时和读取超时时间
     * </p>
     * 
     * @return ClientHttpRequestFactory实例
     */
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（毫秒）- 30秒
        factory.setConnectTimeout(30000);
        // 设置读取超时时间（毫秒）- 10分钟，图像分析可能需要较长时间
        factory.setReadTimeout(600000);
        return factory;
    }
}