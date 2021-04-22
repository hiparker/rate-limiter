## 限流器

代码地址: 

> 限流器正常情况是需要放在流量网关, 比如 OpenResty、Kong等去完成的
大多数会采用 漏桶、令牌桶等算法

> 当前限流器, 则是应对小规模并发在Java中实现 , 只需要在限流的Controller上加上 ```@Limiter```即可

> **限流器规则: IP + URI + QPS令牌桶**

![使用试例1](https://www.bedebug.com/upload/2021/01/%E4%BD%BF%E7%94%A8%E8%AF%95%E4%BE%8B1-1b4a2273752a44fd8616a96225415c7b.png)

![使用试例2](https://www.bedebug.com/upload/2021/01/%E4%BD%BF%E7%94%A8%E8%AF%95%E4%BE%8B2-3715e4938dc446ecbcc68784f0f45661.png)

### 1. 引入谷歌Jar包
```
<dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>29.0-jre</version>
</dependency>

<!-- hutool 工具包 -->
<dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.5.6</version>
</dependency>
```

### 2. 注解类实现
```
package org.opsli.common.annotation.limiter;


import org.opsli.common.enums.AlertType;
import org.opsli.common.utils.RateLimiterUtil;

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
```

### 3. 限流器工具类
```
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opsli.common.thread.refuse.AsyncProcessQueueReFuse;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: think-bboss-parent
 * @BelongsPackage: com.think.bboss.common.utils
 * @Author: Parker
 * @CreateTime: 2021-01-05 16:06
 * @Description: 单机限流
 */
@Slf4j
public final class RateLimiterUtil {

    /** 默认QPS */
    public static final double DEFAULT_QPS = 10d;
    /** 默认缓存个数 超出后流量自动清理 */
    private static final int DEFAULT_CACHE_COUNT = 10_0000;
    /** 默认缓存时效 超出后自动清理 */
    private static final int DEFAULT_CACHE_TIME = 5;
    /** 默认等待时长 */
    private static final int DEFAULT_WAIT = 5000;
    /** 限流器单机缓存 */
    private static final Cache<String, Map<String, RateLimiterInner> > LFU_CACHE;

    static{
        LFU_CACHE = CacheBuilder
                        .newBuilder().maximumSize(DEFAULT_CACHE_COUNT)
                        .expireAfterWrite(DEFAULT_CACHE_TIME, TimeUnit.MINUTES).build();
    }


    /**
     * 删除IP
     * @param ip
     */
    public static void removeIp(String ip) {
        LFU_CACHE.invalidate(ip);
    }

    /**
     * 方法进入
     * @param request
     * @return
     */
    public static boolean enter(HttpServletRequest request) {
        // 获得IP
        String clientIpAddress = IPUtil.getClientIpAddress(request);
        // 获得URI
        String clientURI = request.getRequestURI();
        return RateLimiterUtil.enter(clientIpAddress, clientURI);
    }

    /**
     * 方法进入
     * @param request
     * @return
     */
    public static boolean enter(HttpServletRequest request, Double dfQps) {
        // 获得IP
        String clientIpAddress = IPUtil.getClientIpAddress(request);
        // 获得URI
        String clientURI = request.getRequestURI();
        return RateLimiterUtil.enter(clientIpAddress, clientURI, dfQps);
    }

    /**
     * 方法进入
     * @param clientIpAddress IP
     * @return
     */
    public static boolean enter(String clientIpAddress, String resource) {
        return RateLimiterUtil.enter(clientIpAddress, resource, null);
    }

    /**
     * 方法进入
     * @param clientIpAddress IP
     * @param dfQps 手动指派QPS
     * @return
     */
    public static boolean enter(String clientIpAddress, String resource, Double dfQps) {
        // 计时器
        TimeInterval timer = DateUtil.timer();

        Map<String, RateLimiterInner> rateLimiterInnerMap;
        try {
            rateLimiterInnerMap = LFU_CACHE.get(clientIpAddress, ()->{
                // 当缓存取不到时 重新加载缓存
                Map<String, RateLimiterInner> tmpMap = Maps.newConcurrentMap();
                // 设置限流器
                RateLimiterInner rateLimiterInner = new RateLimiterInner();
                rateLimiterInner.setQps(dfQps);
                rateLimiterInner.setRateLimiter(RateLimiter.create(dfQps));
                tmpMap.put(resource, rateLimiterInner);
                return tmpMap;
            });
        }catch (ExecutionException e){
            log.error(e.getMessage(), e);
            return false;
        }

        RateLimiterInner rateLimiterObj;

        Double qps = dfQps;
        // 初始化过程
        RateLimiterInner rateLimiterInner = rateLimiterInnerMap.get(resource);
        // 如果为空 则创建一个新的限流器
        if(rateLimiterInner == null){
            System.out.println(456);
            rateLimiterInner = new RateLimiterInner();
            rateLimiterInner.setQps(dfQps);
            rateLimiterInner.setRateLimiter(RateLimiter.create(dfQps));
            rateLimiterInnerMap.put(resource, rateLimiterInner);

        }else{
            qps = rateLimiterInner.getQps();
        }
        rateLimiterObj = rateLimiterInner;

        //不限流
        if (qps == null || qps == 0.0) {
            return true;
        }

        RateLimiter rateLimiter = rateLimiterObj.getRateLimiter();

        //非阻塞
        if (!rateLimiter.tryAcquire(Duration.ofMillis(DEFAULT_WAIT))) {
            // 花费毫秒数
            long timerCount = timer.interval();
            //限速中，提示用户
            log.error("限流器 - 访问频繁 耗时: "+ timerCount + "ms, IP地址: " + clientIpAddress + ", URI: " + resource);
            return false;
        } else {
            // 正常访问
            // 花费毫秒数
            long timerCount = timer.interval();
            return true;
        }
    }



    // ==============



    public static void main(String[] args) {
        RateLimiterUtil.removeIp("127.0.0.1");
        for (int i = 0; i < 500; i++) {
            int j = i;
            AsyncProcessQueueReFuse.execute(()->{
                boolean enter = RateLimiterUtil.enter("127.0.0.1","/api/v1", 2d);
                System.out.println(enter);
            });
        }
    }

}

/**
 * 限流器
 */
@Data
class RateLimiterInner {

    /** qps */
    private Double qps;

    /** 限流器 */
    private RateLimiter rateLimiter;

}
```

### 4. AOP拦截器
```

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.opsli.common.annotation.limiter.Limiter;
import org.opsli.common.enums.AlertType;
import org.opsli.common.exception.ServiceException;
import org.opsli.common.utils.OutputStreamUtil;
import org.opsli.common.utils.RateLimiterUtil;
import org.opsli.core.msg.CoreMsg;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static org.opsli.common.constants.OrderConstants.LIMITER_AOP_SORT;
/**
 * 限流器
 *
 * @author 周鹏程
 * @date 2020-09-16
 */
@Slf4j
@Order(LIMITER_AOP_SORT)
@Aspect
@Component
public class LimiterAop {


    @Pointcut("@annotation(org.opsli.common.annotation.limiter.Limiter)")
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
                                    CoreMsg.OTHER_EXCEPTION_LIMITER.getMessage(),
                                    response
                            );
                        }else {
                            // 异常返回
                            throw new ServiceException(CoreMsg.OTHER_EXCEPTION_LIMITER);
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
```

