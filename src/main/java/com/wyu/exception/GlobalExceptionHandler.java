package com.wyu.exception;

import com.wyu.common.Resp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

/**
 * @author novo
 * @date 2023-02-21 13:52
 */
@ControllerAdvice
//@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理未知异常 都是无意义的 不需要提示message 返回模糊信息或记录日志
     *
     * @param request
     * @param e
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public Resp exceptionHandler(HttpServletRequest request, Exception e) {
        String requestUrl = request.getRequestURI();
        String method = request.getMethod();
        log.error("[系统异常] url:[{}]", requestUrl, e);
        return Resp.error("系统异常");
    }

    /**
     * @param request
     * @param e
     * @return
     * @RequestBody注解、Java bean中参数错误产生的异常、表单(Content-Type: application/json、Content-Type: application/xml)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST) // 参数错误 固定返回400
    public Resp methodArgumentNotValidExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException e) {
        String requestUrl = request.getRequestURI();
        String method = request.getMethod();

        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        String errorMsg = this.formatAllErrorMessages(errors);
        log.error("[参数异常] url:[{}],msg:[{}]", requestUrl, errorMsg);
        return Resp.error(errorMsg);
    }

    /**
     * 非Java bean(如路径参数等)参数错误产生的异常
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST) // 参数错误 固定返回400
    public Resp constraintViolationExceptionHandler(HttpServletRequest request, ConstraintViolationException e) {
        String requestUrl = request.getRequestURI();
        String method = request.getMethod();

        // ConstraintViolationException自带的getMessage()也是可以用的，如果对错误信息没有严格的格式要求可以不用通过这种循环来自定义拼接
        StringBuilder errorMsg = new StringBuilder();
        for (ConstraintViolation<?> error : e.getConstraintViolations()) {
            errorMsg.append(error.getMessage()).append(";");
        }
        log.error("[参数异常] url:[{}] msg:[{}]", requestUrl, errorMsg);
        return Resp.error(errorMsg.toString());
    }

    /**
     * Java bean 、表单(Content-Type: multipart/form-data)参数错误产生的异常
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST) // 参数错误 固定返回400
    public Resp bindExceptionExceptionHandler(HttpServletRequest request, BindException e) {
        String requestUrl = request.getRequestURI();
        String method = request.getMethod();
        String errorMsg = formatAllErrorMessages(e.getAllErrors());
        log.error("[参数异常] url:[{}] msg:[{}]", requestUrl, errorMsg);
        return Resp.error(errorMsg);
    }


    /**
     * 拼接所有参数错误信息
     *
     * @param errors
     * @return
     */
    private String formatAllErrorMessages(List<ObjectError> errors) {
        StringBuilder errorMsg = new StringBuilder();
        errors.forEach(objectError ->
                errorMsg.append(objectError.getDefaultMessage()).append(";")
        );
        return errorMsg.toString();
    }
}
