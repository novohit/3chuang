package com.novo.mapper;

import com.novo.pojo.Team;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

/**
 * @author novo
 * @date 2021/11/7-20:35
 */
public interface TeamMapper extends Mapper<Team> {
    @Select("select * FROM team where username=#{username} and phone=#{phone} ")
    Team queryByUsernameAndPhone(@Param("username")String username,@Param("phone") String phone);
    @Update("UPDATE team set resources=#{urls},create_time=#{create_time} where username=#{username} and phone=#{phone}")
    void updateByUsernameAndPhone(@Param("urls") String urls,
                                  @Param("username") String username,
                                  @Param("phone") String phone,
                                  @Param("create_time") Date date);

    @Select("select resources FROM team ")
    List<String> selectAllUrls();
}
