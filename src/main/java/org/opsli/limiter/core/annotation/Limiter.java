package org.opsli.limiter.core.annotation;



import org.opsli.limiter.core.enums.AlertType;
import org.opsli.limiter.core.util.RateLimiterUtil;

import java.lang.annotation.*;

/**
 * Java 限流器
 * @author Parker
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limiter {

    /** QPS */
    double qps() default RateLimiterUtil.DEFAULT_QPS;

    /** 提醒方式 */
    AlertType alertType() default AlertType.JSON;

}
