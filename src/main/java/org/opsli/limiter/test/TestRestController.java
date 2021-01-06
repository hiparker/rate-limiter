package org.opsli.limiter.test;

import org.opsli.limiter.core.annotation.Limiter;
import org.opsli.limiter.core.ret.ResultVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: rate-limiter
 * @BelongsPackage: org.opsli.limiter.test
 * @Author: Parker
 * @CreateTime: 2021-01-06 17:56
 * @Description: 测试
 */
@RestController
public class TestRestController {

    /**
     * 测试 限流器
     * @return
     */
    @Limiter
    @GetMapping("/test")
    public ResultVo<?> test(){
        return ResultVo.success("HelloWorld!");
    }

}
