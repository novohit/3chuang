package com.novo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author novo
 * @date 2021/11/8-21:52
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求放行
        if(request.getMethod().toUpperCase().equals("OPTIONS")){
            return true;
        }
        Object managerName = request.getSession().getAttribute("managerName");
        if (managerName==null){
            request.setAttribute("msg","没有权限，请先登录");
            System.out.println("没有权限，请先登录");
            return false;
        }
        return true;
    }
}
