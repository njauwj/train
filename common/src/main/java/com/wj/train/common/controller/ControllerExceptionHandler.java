package com.wj.train.common.controller;

import com.wj.train.common.exception.BusinessException;
import com.wj.train.common.resp.CommonResp;
import com.wj.train.common.utils.RespUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理、数据预处理等
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    /**
     * 所有异常统一处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommonResp<Object> exceptionHandler(Exception e) {
        LOG.error("系统异常：", e);
        return RespUtil.error("系统出现异常，请联系管理员");
    }

    /**
     * 业务异常统一处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public CommonResp<Object> exceptionHandler(BusinessException e) {
        LOG.error("业务异常：{}", e.getE().getDesc());
        return RespUtil.error(e.getE().getDesc());
    }

    /**
     * 校验异常统一处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public CommonResp<Object> exceptionHandler(BindException e) {
        LOG.error("校验异常：{}", e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return RespUtil.error(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
