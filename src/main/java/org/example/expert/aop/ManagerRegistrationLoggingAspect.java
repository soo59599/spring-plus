package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.entity.Log.LogResult;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ManagerRegistrationLoggingAspect {

    private final LogService logService;

    @Around("execution(* org.example.expert.domain.manager.controller.ManagerController.saveManager(..))")
    public Object logManagerRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();


        Long requestUserId = null;
        Long todoId = null;
        Long managerUserId = null;

        if (args.length > 2
                && args[0] instanceof AuthUser authUser
                && args[1] instanceof Long todoIdParam
                && args[2] instanceof ManagerSaveRequest managerSaveRequest) {

            requestUserId = authUser.getId();
            todoId = todoIdParam;
            managerUserId = managerSaveRequest.getManagerUserId();
        }

        try {
            Object result = joinPoint.proceed();
            logService.saveLog(requestUserId, managerUserId, todoId, LogResult.SUCCESS);
            log.info(LogResult.SUCCESS.getMessage());
            return result;
        } catch (Exception e) {
            try {
                logService.saveLog(requestUserId, managerUserId, todoId, LogResult.FAIL);
                log.info(LogResult.FAIL.getMessage());
            } catch (Exception logException) {
                // 로그 저장 실패
                log.error("로그 저장 실패: {}", logException.getMessage());
            }

            // 원본 예외는 그대로 던짐
            throw e;
        }
    }

}
