package com.alibaba.dbhub.server.start.exception;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.dbhub.server.start.exception.convertor.BindExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.BusinessExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.DefaultExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.ExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.MaxUploadSizeExceededExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.MethodArgumentNotValidExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.MethodArgumentTypeMismatchExceptionConvertor;
import com.alibaba.dbhub.server.start.exception.convertor.ParamExceptionConvertor;
import com.alibaba.dbhub.server.tools.base.excption.BusinessException;
import com.alibaba.dbhub.server.tools.base.excption.SystemException;
import com.alibaba.dbhub.server.tools.base.wrapper.result.ActionResult;
import com.alibaba.dbhub.server.tools.common.exception.NeedLoggedInBusinessException;
import com.alibaba.dbhub.server.tools.common.exception.RedirectBusinessException;
import com.alibaba.fastjson2.JSON;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 拦截Controller异常
 *
 * @author 是仪
 */
@ControllerAdvice
@Slf4j
public class EasyControllerExceptionHandler {

    /**
     * 所有的异常处理转换器
     */
    public static final Map<Class<?>, ExceptionConvertor> EXCEPTION_CONVERTOR_MAP = Maps.newHashMap();

    static {
        EXCEPTION_CONVERTOR_MAP.put(MethodArgumentNotValidException.class,
            new MethodArgumentNotValidExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(BindException.class, new BindExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(BusinessException.class, new BusinessExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(NeedLoggedInBusinessException.class, new BusinessExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(MissingServletRequestParameterException.class, new ParamExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(IllegalArgumentException.class, new ParamExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(MethodArgumentTypeMismatchException.class,
            new MethodArgumentTypeMismatchExceptionConvertor());
        EXCEPTION_CONVERTOR_MAP.put(MaxUploadSizeExceededException.class,
            new MaxUploadSizeExceededExceptionConvertor());
    }

    /**
     * 默认转换器
     */
    public static ExceptionConvertor DEFAULT_EXCEPTION_CONVERTOR = new DefaultExceptionConvertor();

    /**
     * 业务异常
     *
     * @param request   request
     * @param exception exception
     * @return return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class,
        MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
        BusinessException.class, MaxUploadSizeExceededException.class, ClientAbortException.class,
        HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotAcceptableException.class,
        MultipartException.class, MissingRequestHeaderException.class, HttpMediaTypeNotSupportedException.class,
        NeedLoggedInBusinessException.class})
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ActionResult handleBusinessException(HttpServletRequest request, Exception exception) {
        ActionResult result = convert(exception);
        log.info("发生业务异常{}:{}", request.getRequestURI(), result, exception);
        return result;
    }

    /**
     * 业务异常
     *
     * @param request   request
     * @param exception exception
     * @return return
     */
    @ExceptionHandler({RedirectBusinessException.class})
    public ModelAndView handleModelAndViewBizException(HttpServletRequest request, Exception exception) {
        ModelAndView result = translateModelAndView(exception);
        log.info("发生ModelAndView业务异常{}:{}", request.getRequestURI(), result, exception);
        return result;
    }

    public ModelAndView translateModelAndView(Throwable exception) {
        // 参数异常
        if (exception instanceof RedirectBusinessException) {
            RedirectBusinessException e = (RedirectBusinessException)exception;
            return dealResponseModelAndView(null, e.getMessage(), e.getRedirect(), null, null);
        }
        // 默认跳首页
        return new ModelAndView("redirect:/");
    }

    private ModelAndView dealResponseModelAndView(String title, String errorMessage, String redirect, String href,
        String buttonText) {
        // 如果有车重定向信息 则跳转
        if (StringUtils.isNotBlank(redirect)) {
            return new ModelAndView("redirect:" + redirect);
        }
        // 默认跳首页
        return new ModelAndView("redirect:/");

        // 同步请求
        //return ModelAndViewUtils.error(title, errorMessage,href,buttonText);
    }

    /**
     * 系统异常
     *
     * @param request   request
     * @param exception exception
     * @return return
     */
    @ExceptionHandler({SystemException.class})
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ActionResult handleSystemException(HttpServletRequest request, Exception exception) {
        ActionResult result = convert(exception);
        log.error("发生业务异常{}:{}", request.getRequestURI(), result, exception);
        return result;
    }

    /**
     * 未知异常 需要人工介入查看日志
     *
     * @param request   request
     * @param exception exception
     * @return return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ActionResult handledException(HttpServletRequest request, Exception exception) {
        ActionResult result = convert(exception);
        log.error("发生未知异常{}:{},请求参数:{}", request.getRequestURI(), result,
            JSON.toJSONString(request.getParameterMap()),
            exception);
        return result;
    }

    public ActionResult convert(Throwable exception) {
        ExceptionConvertor exceptionConvertor = EXCEPTION_CONVERTOR_MAP.get(exception.getClass());
        if (exceptionConvertor == null) {
            exceptionConvertor = DEFAULT_EXCEPTION_CONVERTOR;
        }
        return exceptionConvertor.convert(exception);
    }
}
