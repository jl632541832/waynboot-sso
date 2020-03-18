package com.wayn.mall.exception;

import com.wayn.mall.base.BaseController;
import com.wayn.mall.util.R;
import com.wayn.mall.util.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice extends BaseController {


    /**
     * 处理404异常
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handle404Exception(NoHandlerFoundException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        if (HttpUtil.isAjax(request)) {
            return R.error("您请求路径不存在，请检查url！");
        }
        return new ModelAndView("error/404");
    }

    /**
     * 处理自定义异常
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({BusinessException.class})
    public Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        if (HttpUtil.isAjax(request)) {
            return R.error(e.getMessage());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("msg", e.getMessage());
        return new ModelAndView("error/500", map);
    }

    @ExceptionHandler({Exception.class})
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        if (HttpUtil.isAjax(request)) {
            return R.error("服务器内部错误！");
        }
        return new ModelAndView("error/500");
    }
}
