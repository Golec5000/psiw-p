package com.psiw.proj.backend.utils.aspects;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecution {
}
