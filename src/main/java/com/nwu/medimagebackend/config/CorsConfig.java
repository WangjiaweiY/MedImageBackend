package com.nwu.medimagebackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享(CORS)配置类
 * <p>
 * 该配置类负责处理跨域资源共享(CORS)相关的配置，允许前端应用从不同的域名访问后端API和资源。
 * 集中配置CORS策略，替代在每个控制器上使用@CrossOrigin注解的方式，便于统一管理和维护。
 * </p>
 * <p>
 * 关键功能：
 * <ul>
 *   <li>支持配置多个允许的源模式（Origin Patterns）</li>
 *   <li>控制允许的HTTP方法</li>
 *   <li>配置请求头和响应头</li>
 *   <li>设置凭证支持（Credentials）</li>
 *   <li>配置预检请求（Preflight）缓存时间</li>
 * </ul>
 * </p>
 * <p>
 * 注意：当allowCredentials设为true时，不能使用通配符"*"作为allowedOrigins，
 * 必须使用allowedOriginPatterns并指定具体的域名模式。
 * </p>
 * 
 * @author MedImage团队
 * @see org.springframework.web.filter.CorsFilter
 * @see org.springframework.web.cors.CorsConfiguration
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * 允许的源模式，支持多个源，以逗号分隔
     * 例如："http://localhost:5173,http://127.0.0.1:5173"
     */
    @Value("${cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    /**
     * 允许的HTTP方法，以逗号分隔
     * 例如："GET,POST,PUT,DELETE,OPTIONS"
     */
    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    /**
     * 允许的HTTP请求头，以逗号分隔，可以使用"*"表示所有
     */
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    /**
     * 是否允许发送凭证（如cookies）
     * 当设置为true时，allowedOriginPatterns不能使用通配符"*"
     */
    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    /**
     * 预检请求的有效期，单位为秒
     * 浏览器会缓存预检请求的结果，在有效期内不再发送预检请求
     */
    @Value("${cors.max-age}")
    private long maxAge;

    /**
     * 创建并配置CORS过滤器
     * <p>
     * 该Bean会被Spring容器自动注册为Filter，处理所有经过的请求，
     * 添加适当的CORS响应头，以允许跨域请求。
     * </p>
     *
     * @return 配置好的CorsFilter实例
     */
    @Bean
    public CorsFilter corsFilter() {
        log.info("=== 初始化CORS过滤器 ===");
        log.info("允许的源模式: {}", allowedOriginPatterns);
        log.info("允许的HTTP方法: {}", allowedMethods);
        log.info("允许的请求头: {}", allowedHeaders);
        log.info("允许凭证: {}", allowCredentials);
        log.info("预检请求有效期: {} 秒", maxAge);
        
        CorsConfiguration config = new CorsConfiguration();
        
        // 设置允许的源模式
        String[] patterns = allowedOriginPatterns.split(",");
        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (!pattern.isEmpty()) {
                config.addAllowedOriginPattern(pattern);
                log.debug("添加允许的源模式: {}", pattern);
            }
        }
        
        // 设置允许的HTTP方法
        for (String method : allowedMethods.split(",")) {
            method = method.trim();
            if (!method.isEmpty()) {
                config.addAllowedMethod(method);
                log.debug("添加允许的HTTP方法: {}", method);
            }
        }
        
        // 设置允许的请求头
        config.addAllowedHeader("*");
        log.debug("添加允许的请求头: *");
        
        // 允许凭证
        config.setAllowCredentials(allowCredentials);
        
        // 预检请求有效期
        config.setMaxAge(maxAge);
        
        // 创建URL匹配源并注册配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        log.info("CORS过滤器配置完成，应用于所有路径 (/**)");
        
        return new CorsFilter(source);
    }
} 