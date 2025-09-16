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
    private final HttpServletRequest request;

    @Around("execution(* org.example.expert.domain.manager.controller.ManagerController.saveManager(..))")
    public Object logManagerRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();


        Long requestUserId = null;
        Long todoId = null;
        Long managerUserId = null;

        // SecurityContext에서 현재 사용자 ID 가져오기
        for (Object arg : args) {
            if (arg instanceof AuthUser authUser) {
                requestUserId = authUser.getId();
                break;
            }
        }

        if (args.length > 2) {
            Object secondParam = args[1];
            Object thirdParam = args[2];

            if (thirdParam instanceof ManagerSaveRequest managerSaveRequest && secondParam instanceof Long) {
                todoId = (Long) secondParam;
                managerUserId = managerSaveRequest.getManagerUserId();
            }
        }

        try {
            Object result = joinPoint.proceed();
            logService.saveLog(requestUserId, managerUserId, todoId, LogResult.SUCCESS);
            return result;
        } catch (Exception e) {
            logService.saveLog(requestUserId, managerUserId, todoId, LogResult.FAIL);
            throw e;
        }
    }

}
