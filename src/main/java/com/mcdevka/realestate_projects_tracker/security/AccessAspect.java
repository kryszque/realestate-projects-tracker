package com.mcdevka.realestate_projects_tracker.security;

import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.security.annotation.CheckAccess;
import com.mcdevka.realestate_projects_tracker.security.annotation.ProjectId;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class AccessAspect {

    private final AccessControlService accessControlService;

    @Before("@annotation(annotation)")
    public void checkAccess(JoinPoint joinPoint, CheckAccess annotation) {

        Long projectId = extractProjectId(joinPoint);

        if (projectId != null) {
            ProjectPermissions requiredPermission = annotation.value();
            accessControlService.checkAccess(projectId, requiredPermission);
        } else {
            accessControlService.checkAccessWithoutProjectId(annotation.value());
            //throw new IllegalStateException("Method with @CheckAccess has to have 'projectId'!");
        }
    }

    private Long extractProjectId(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof ProjectId) {
                    if (args[i] instanceof Long) {
                        return (Long) args[i];
                    }
                }
            }
        }

        String[] parameterNames = signature.getParameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            if ("projectId".equals(parameterNames[i])) {
                if (args[i] instanceof Long) return (Long) args[i];
            }
        }
        throw new IllegalStateException("@ProjectId annotation not found and method does not have pram projectId");
    }
}