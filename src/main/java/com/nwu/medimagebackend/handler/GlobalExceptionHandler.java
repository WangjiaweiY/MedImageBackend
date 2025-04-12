package com.nwu.medimagebackend.handler;

import com.nwu.medimagebackend.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 统一拦截并处理系统中抛出的各类异常，将异常信息转换为标准的响应格式返回给客户端。
 * 通过集中处理异常，避免在每个控制器中重复编写异常处理逻辑，提高代码复用性和可维护性。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>记录异常信息到日志系统</li>
 *   <li>将异常转换为友好的错误消息</li>
 *   <li>保持统一的错误响应格式</li>
 * </ul>
 * </p>
 * 
 * @author MedImage团队
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 通用异常处理方法
     * <p>
     * 捕获并处理控制器中抛出的所有未被特定处理器捕获的异常。
     * </p>
     * 
     * @param ex 捕获到的异常对象
     * @return 标准格式的错误响应
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception ex){
        log.error("系统异常: {}", ex.getMessage(), ex);
        return Result.error(ex.getMessage());
    }

    /**
     * SQL约束违反异常处理方法（当前已注释）
     * <p>
     * 专门处理数据库约束违反异常，例如唯一键冲突等。
     * 当需要使用时可取消注释并实现相应逻辑。
     * </p>
     * 
     * @param ex SQL约束违反异常
     * @return 标准格式的错误响应
     */
//    @ExceptionHandler
//    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
//        String message = ex.getMessage();
//        if(message.contains("Duplicate entry")){
//            String[] split = message.split(" ");
//            String username = split[2];
//            String msg = username + MessageConstant.ALREADY_EXISTS;
//            log.warn("数据库唯一约束冲突: {}", msg);
//            return Result.error(msg);
//        }else {
//            log.error("未知的SQL约束异常: {}", message, ex);
//            return Result.error(MessageConstant.UNKNOWN_ERROR);
//        }
//    }
}
