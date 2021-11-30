package com.novo;

import com.novo.service.ManagerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegisterApplicationTests {
    @Autowired
    private ManagerService userService;
    @Test
    void contextLoads() {
        userService.findAll();
    }

}
