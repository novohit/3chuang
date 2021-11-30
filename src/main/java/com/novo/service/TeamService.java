package com.novo.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.novo.controller.TeamController;
import com.novo.mapper.TeamMapper;
import com.novo.common.PageResult;
import com.novo.pojo.Team;
import com.novo.utils.FileConvertUtil;
import javafx.collections.transformation.FilteredList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author novo
 * @date 2021/11/7-20:35
 */
@Service
public class TeamService {
    private static final int ZIP_BUFFER_SIZE = 8192;
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamController.class);
    private static final String FILE_PATH = "/root/file/";
    private static final List<String> CONTENT_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/x-zip-compressed",
            "application/zip",
            "application/vnd.rar",
            "application/octet-stream");

    @Autowired
    private TeamMapper teamMapper;

    public PageResult<Team> queryTeamsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Team.class);
        Example.Criteria criteria = example.createCriteria();
        //添加模糊查询
        if (StringUtils.isNotBlank(key)) {//key 不为空才进行模糊查询 否则查询所有
            //name模糊 品牌首字母精确查询 or关系
            criteria.andLike("username", "%" + key + "%").orEqualTo("phone", key);
        }
        //添加分页
        PageHelper.startPage(page, rows);
        //添加排序
        if (StringUtils.isNotBlank(sortBy)) {
            //sql语句 order by 排序字段 desc    注意：desc和asc前面有个空格！！！
            //如果desc为true则降序
            example.setOrderByClause(sortBy + (desc ? " desc" : " asc"));
        }
        //找到适合的通用mapper方法 执行
        List<Team> teams = this.teamMapper.selectByExample(example);
        teams.forEach(team -> {
            team.setResources(team.getResources().split("/")[3].split("\"")[0]);
        });


        PageInfo<Team> pageInfo = new PageInfo<>(teams);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(),pageInfo.getPages());
    }

    public String upload(String username, String phone, MultipartFile file) {
        //1 校验文件类型 (后缀或者contentType)

        String filename = file.getOriginalFilename();
        filename=username+"_"+phone+filename;

        try {
            filename = new String(filename.getBytes(), "UTF-8");
            System.out.println(filename);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //2 暂定根据重复文件名删除
        String url = FILE_PATH + filename;
        File file1 = new File(url);
        if (file1.exists()) {
            file1.delete();
            System.out.println("===========重新上传=================");
        }
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)) {
            //日志写法
            LOGGER.info("文件上传失败：{},文件类型不合法！", filename);//占位符写法 效率高
            return null;
        }
        try {
            //3 校验文件内容
            //4 保存到服务器
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(url));
            //file.transferTo(new File(url));
            //5 返回url路径
            System.out.println("返回url路径" + FILE_PATH + filename);
            return FILE_PATH + filename;
        } catch (IOException e) {
            LOGGER.info("文件上传失败,服务器异常！");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 上传队伍信息
     *
     * @param username
     * @param phone
     * @param urls
     * @return
     */
    @Transactional
    public Team saveTeam(String username, String phone, List<String> urls) {
        System.out.println(urls);
        Team team = this.teamMapper.queryByUsernameAndPhone(username, phone);
        if (team != null) {
            this.teamMapper.updateByUsernameAndPhone(JSON.toJSONString(urls), username, phone, new Date());
            return team;//这里返回的是旧信息
        } else {
            Team team1 = new Team();
            team1.setId(null);
            team1.setUsername(username);
            team1.setPhone(phone);
            team1.setResources(JSON.toJSONString(urls));
            team1.setDate(new Date());
            this.teamMapper.insertSelective(team1);
            return team1;
        }
    }

    public String onlinePreview(String url, HttpServletResponse response) throws Exception {
        //获取文件类型
        String[] str = StringUtils.split(url, "\\.");

        if (str.length == 0) {
            throw new Exception("文件格式不正确");
        }
        String suffix = str[str.length - 1];
        if (!suffix.equals("txt") && !suffix.equals("doc") && !suffix.equals("docx") && !suffix.equals("xls")
                && !suffix.equals("xlsx") && !suffix.equals("ppt") && !suffix.equals("pptx")) {
            throw new Exception("文件格式不支持预览");
        }
        InputStream in = FileConvertUtil.convertLocaleFile(url, suffix);
        OutputStream outputStream = response.getOutputStream();
        //创建存放文件内容的数组
        byte[] buff = new byte[1024];
        //所读取的内容使用n来接收
        int n;
        //当没有读取完时,继续读取,循环
        while ((n = in.read(buff)) != -1) {
            //将字节数组的数据全部写入到输出流中
            outputStream.write(buff, 0, n);
        }
        //强制将缓存区的数据进行输出
        outputStream.flush();
        //关流
        outputStream.close();
        in.close();
        return "success";
    }

    public String downloadOne(HttpServletResponse response, String url) throws UnsupportedEncodingException {
        System.out.println(url);
        File file = new File(url);
        System.out.println(file.getPath());
       /* if(!file.exists()){
            return "下载文件不存在";
        }*/
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(file.getName().getBytes("utf-8"), "ISO8859-1"));
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            LOGGER.info("文件下载失败,服务器异常！");
            e.printStackTrace();
        }
        return "文件下载成功";
    }

    public HttpHeaders downloadAll(List<String> list, String resourcesName) throws IOException {
        fileToZip(list, resourcesName);
        HttpHeaders headers = new HttpHeaders();
        String filename = new String(resourcesName.getBytes("utf-8"), "iso-8859-1");
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }

    private String fileToZip(List<String> list, String resourcesName) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(FILE_PATH + resourcesName));
        InputStream input = null;

        for (String str : list) {
            String name = str;
            System.out.println(str);
            input = new FileInputStream(new File(name));
            zipOut.putNextEntry(new ZipEntry(str));
            int temp = 0;
            while ((temp = input.read()) != -1) {
                zipOut.write(temp);
            }
            input.close();
        }
        zipOut.close();
        return null;
    }

    /**
     * 删除文件
     *
     * @param fileList
     * @return
     */
    public static void deleteFile(List<File> fileList) {
        for (File file : fileList) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public Integer deleteTeam(Integer id) {
        return this.teamMapper.deleteByPrimaryKey(id);
    }

    public List<Team> selectAll() {
        Example example =new Example(Team.class);

        //这里注意 要写实体类的属性名 而不是数据库的字段名 create_time
        example.orderBy("date");
        List<Team> teams = this.teamMapper.selectByExample(example);
        teams.forEach(team -> {
            team.setResources(team.getResources().split("/")[3].split("\"")[0]);
        });
        return teams;
    }

    public List<String> selectAllUrls() {
        return this.teamMapper.selectAllUrls();
    }
}
