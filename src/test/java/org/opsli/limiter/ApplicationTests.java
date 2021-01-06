package org.opsli.limiter;

import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ApplicationTests {



    @Test
    void contextLoads() {

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < 1000; i++) {
            executorService.execute(()->{
                try {
                    String ret = HttpUtil.get("http://127.0.0.1:7000/test");
                    System.out.println(ret);
                } catch (Exception e) {
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
    }

}
