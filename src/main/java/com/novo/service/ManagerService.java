package com.novo.service;

import com.novo.mapper.ManagerMapper;
import com.novo.pojo.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author novo
 * @date 2021/11/7-18:44
 */
@Service
public class ManagerService {
    @Autowired
    private ManagerMapper managerMapper;

    public List<Manager> findAll(){
        List<Manager> list = managerMapper.selectAll();
        System.out.println(list);
        return list;
    }

    public Integer login(String managerName, String password) {
        return this.managerMapper.login(managerName,password);
    }
}
