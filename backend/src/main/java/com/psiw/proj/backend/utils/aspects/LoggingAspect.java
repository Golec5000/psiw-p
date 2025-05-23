package com.psiw.proj.backend.utils.aspects;

import org.springframework.util.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@within(com.psiw.proj.backend.utils.aspects.LogExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        Object[] args = joinPoint.getArgs();

        logger.info("▶️ Wywołanie metody: {}.{}() z argumentami: {}", className, methodName, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            logger.info("✅ Metoda: {}.{}() zakończyła się sukcesem w czasie: {} ms, zwrócono: {}", className, methodName, stopWatch.getTotalTimeMillis(), result);
            return result;

        } catch (Throwable throwable) {
            stopWatch.stop();
            logger.error("❌ Metoda: {}.{}() zakończyła się błędem po czasie: {} ms, błąd: {}", className, methodName, stopWatch.getTotalTimeMillis(), throwable.getMessage(), throwable);
            throw throwable;
        }
    }
}
