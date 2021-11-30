package com.novo.mapper;

import com.novo.pojo.Manager;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author novo
 * @date 2021/11/7-18:38
 */
public interface ManagerMapper extends Mapper<Manager> {
    @Select("SELECT * FROM `manager` where manager_name =#{managerName} AND password =#{password}")
    Integer login(@Param("managerName") String managerName,@Param("password") String password);
}
