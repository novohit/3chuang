package com.wyu.mapper;

import com.wyu.model.Team;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author wyu
 * @date 2021/11/7-20:35
 */
public interface TeamMapper extends Mapper<Team> {
    @Select("select * FROM team where username=#{username} and phone=#{phone} ")
    Team queryByUsernameAndPhone(@Param("username") String username, @Param("phone") String phone);

    @Update("UPDATE team set resources=#{urls} where username=#{username} and phone=#{phone}")
    void updateByUsernameAndPhone(@Param("urls") String urls,
                                  @Param("username") String username,
                                  @Param("phone") String phone);

    @Select("select resources FROM team ")
    List<String> selectAllUrls();

    @Select({"<script>",
            " SELECT ",
            " resources ",
            " FROM team WHERE id in ",
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<String> selectUrlIn(@Param("ids") List<Integer> ids);
}
