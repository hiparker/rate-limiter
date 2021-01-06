/**
 * Copyright 2020 OPSLI 快速开发平台 https://www.opsli.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opsli.limiter.core.aop;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.opsli.limiter.core.annotation.Limiter;
import org.opsli.limiter.core.enums.AlertType;
import org.opsli.limiter.core.exception.ServiceException;
import org.opsli.limiter.core.msg.CommonMsg;
import org.opsli.limiter.core.util.OutputStreamUtil;
import org.opsli.limiter.core.util.RateLimiterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


/**
 * 限流器
 *
 * @author 周鹏程
 * @date 2020-09-16
 */
@Aspect
@Component
public class LimiterAop {

    private static final Logger log = LoggerFactory.getLogger(LimiterAop.class);

    @Pointcut("@annotation(org.opsli.limiter.core.annotation.Limiter)")
    public void requestMapping() {
    }

    /**
     * 限流
     * @param point
     */
    @Before("requestMapping()")
    public void limiterHandle(JoinPoint point){
        try {
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes sra = (ServletRequestAttributes) ra;
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            if(sra != null) {
                HttpServletRequest request = sra.getRequest();
                HttpServletResponse response = sra.getResponse();
                Limiter limiter = method.getAnnotation(Limiter.class);
                if(limiter != null){
                    AlertType alertType = limiter.alertType();
                    double qps = limiter.qps();

                    // 限流
                    boolean enterFlag = RateLimiterUtil.enter(request, qps);
                    if(!enterFlag){
                        // alert 弹出
                        if(AlertType.ALERT == alertType){
                            OutputStreamUtil.exceptionResponse(
                                    CommonMsg.OTHER_EXCEPTION_LIMITER.getMessage(),
                                    response
                            );
                        }else {
                            // 异常返回
                            throw new ServiceException(CommonMsg.OTHER_EXCEPTION_LIMITER);
                        }
                    }
                }
            }
        }catch (ServiceException e){
            throw e;
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

}
