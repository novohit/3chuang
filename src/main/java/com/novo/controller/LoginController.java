package com.novo.controller;

import com.novo.service.ManagerService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

/**
 * @author novo
 * @date 2021/11/8-20:49
 */
@Controller
public class LoginController {
    @Autowired
    private ManagerService managerService;
    @PostMapping("/login")
    public ResponseEntity<String> doLogin(@RequestParam("managerName") String managerName,@RequestParam("password")String password, HttpSession session){
        System.out.println(managerName+password);
        if(StringUtils.isBlank(managerName)||StringUtils.isBlank(password)){
            return new ResponseEntity<String>("请填写用户名和密码", HttpStatus.BAD_REQUEST);
        }
        Integer result = this.managerService.login(managerName, password);
        if(result==null||result.intValue()!=1){
            return new ResponseEntity<>("用户名或密码错误",HttpStatus.UNAUTHORIZED);
        }
            //将登陆信息放入session
            session.setAttribute("managerName",managerName);
        return ResponseEntity.ok("登陆成功");
    }
}
