package com.novo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author novo
 * @date 2021/11/8-22:00
 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer{
    @Resource
    private LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 对部分访问进行拦截
         */
      /*  registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") //先对所有部分进行拦截
                .excludePathPatterns( //再排除不需要拦截的部分
                        "/login",
                        "/team/onlinePreview",
                        "/swagger-resources/**",
                        "/swagger-ui.html#/**");*/
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(
                Charset.forName("UTF-8"));
        converters.add(converter);
    }
}
