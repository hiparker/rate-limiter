package org.opsli.limiter.core.util;

import cn.hutool.core.io.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @BelongsProject: think-bboss-parent
 * @BelongsPackage: com.think.bboss.common.utils
 * @Author: Parker
 * @CreateTime: 2021-01-05 14:26
 * @Description: 周鹏程
 */
public final class OutputStreamUtil {

    private static final Logger log = LoggerFactory.getLogger(OutputStreamUtil.class);


    /**
     * 返回异常值
     */
    public static void exceptionResponse(String msg, HttpServletResponse response){
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8;");
            PrintWriter writer = response.getWriter();
            writer.write(
                    "<script type=\"text/javascript\">alert('"+msg+"');</script>");
            writer.flush();
            // 关闭流
            IoUtil.close(writer);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }


    // ==========================

    private OutputStreamUtil(){}

}
