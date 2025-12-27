package com.github.order_manager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut for all methods in controller package
     */
    @Pointcut("execution(* com.github.order_manager.controller..*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut for all methods in service package
     */
    @Pointcut("execution(* com.github.order_manager.service..*(..))")
    public void serviceMethods() {}

    /**
     * Log method entry with parameters
     */
    @Before("controllerMethods() || serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("{} :: {} :: Entry :: args={}",
                className.substring(className.lastIndexOf('.') + 1),
                methodName,
                Arrays.toString(args));
    }

    /**
     * Log method execution with return value
     */
    @Around("controllerMethods() || serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("{} :: {} :: Exit :: executionTime={}ms :: result={}",
                    className.substring(className.lastIndexOf('.') + 1),
                    methodName,
                    executionTime,
                    result);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            log.error("{} :: {} :: Exception :: executionTime={}ms :: error={}",
                    className.substring(className.lastIndexOf('.') + 1),
                    methodName,
                    executionTime,
                    e.getMessage());

            throw e;
        }
    }

    /**
     * Log exceptions thrown from controller or service methods
     */
    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.error("{} :: {} :: Exception thrown :: {}",
                className.substring(className.lastIndexOf('.') + 1),
                methodName,
                exception.getMessage(),
                exception);
    }
}
