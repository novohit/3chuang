package com.wyu.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wyu.common.PageResult;
import com.wyu.component.OSSComponent;
import com.wyu.mapper.TeamMapper;
import com.wyu.model.Team;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author wyu
 * @date 2021/11/7-20:35
 */
@Service
public class TeamService {

    @Autowired
    private OSSComponent OSSComponent;

    @Autowired
    private TeamMapper teamMapper;

    public PageResult<Team> queryTeamsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Team.class);
        Example.Criteria criteria = example.createCriteria();
        //添加模糊查询
        if (StringUtils.isNotBlank(key)) {//key 不为空才进行模糊查询 否则查询所有
            criteria.andLike("username", "%" + key + "%").orLike("phone", "%" + key + "%");
        }
        //添加分页
        PageHelper.startPage(page, rows);
        //添加排序
        //if (StringUtils.isNotBlank(sortBy)) {
        //sql语句 order by 排序字段 desc    注意：desc和asc前面有个空格
        //如果desc为true则降序
        //example.setOrderByClause(sortBy + (desc ? " desc" : " asc"));
        example.setOrderByClause("create_time" + " desc");

        //找到适合的通用mapper方法 执行
        List<Team> teams = this.teamMapper.selectByExample(example);

        PageInfo<Team> pageInfo = new PageInfo<>(teams);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPages());
    }

    public String upload(String username, String phone, MultipartFile file) {
        String fileName = username + "_" + phone + "_" + file.getOriginalFilename();
        return this.OSSComponent.upload(file, fileName);
    }

    public void download(HttpServletResponse response, List<Integer> ids, Boolean all) {
        List<String> fileUrls;
        if (all) {
            fileUrls = this.teamMapper.selectAllUrls();
        } else {
            fileUrls = this.teamMapper.selectUrlIn(ids);
        }
        List<String> fileNames = fileUrls.stream()
                .map(url -> url.replace("http://novohit.oss-cn-guangzhou.aliyuncs.com/", ""))
                .collect(Collectors.toList());
        // "http://novohit.oss-cn-guangzhou.aliyuncs.com/3chuang/2023/02/28/%E6%B5%8B%E8%AF%952_13211291857_nginx.txt"
        this.OSSComponent.getFileToZip(fileNames, response);
    }

    public Team saveTeam(String username, String phone, String url) {
        Team team = this.teamMapper.queryByUsernameAndPhone(username, phone);
        if (team != null) {
            this.teamMapper.updateByUsernameAndPhone(url, username, phone);
        } else {
            team = new Team();
            team.setUsername(username);
            team.setPhone(phone);
            team.setResources(url);
            this.teamMapper.insertSelective(team);
        }
        return team;
    }

    public Integer deleteTeam(Integer id) {
        return this.teamMapper.deleteByPrimaryKey(id);
    }

    public List<Team> selectAll() {
        Example example = new Example(Team.class);

        //这里注意 OrderBy方法要写实体类的属性名 而不是数据库的字段名 create_time  setOrderByClause方法写数据库字段名
        example.setOrderByClause("create_time" + " desc");
        return this.teamMapper.selectByExample(example);
    }

    public List<String> selectAllUrls() {
        return this.teamMapper.selectAllUrls();
    }
}
