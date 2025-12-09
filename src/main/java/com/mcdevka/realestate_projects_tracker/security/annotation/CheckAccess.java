package com.mcdevka.realestate_projects_tracker.security.annotation;

import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAccess {
    ProjectPermissions value() default ProjectPermissions.CAN_VIEW;
}
