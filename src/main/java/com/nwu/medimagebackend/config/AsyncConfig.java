package com.nwu.medimagebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 * <p>
 * 配置异步任务执行器，用于处理异步图像分析任务
 * </p>
 * 
 * @author MedImage团队
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 创建异步任务执行器
     * <p>
     * 配置线程池参数，用于执行异步分析任务
     * </p>
     * 
     * @return 异步任务执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数，保持活跃的线程数量
        executor.setCorePoolSize(3);
        // 最大线程数，当队列满时可以开启的最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量，用于缓存任务的队列大小
        executor.setQueueCapacity(50);
        // 线程前缀，用于区分不同的线程池
        executor.setThreadNamePrefix("fullnet-analysis-");
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化线程池
        executor.initialize();
        return executor;
    }
} 