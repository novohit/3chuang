package com.wyu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableSwagger2
@MapperScan("com.wyu.mapper")
public class ThreeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreeApplication.class, args);
    }

}
