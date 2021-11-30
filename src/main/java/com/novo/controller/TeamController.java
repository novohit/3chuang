package com.novo.controller;

import com.alibaba.fastjson.JSONObject;
import com.novo.common.PageResult;
import com.novo.pojo.Team;
import com.novo.service.TeamService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.*;

import java.util.ArrayList;
import java.util.List;


import static com.novo.utils.FIleToZipUtil.downLoadFiles;
import static com.novo.utils.FIleToZipUtil.downloadZip;


/**
 * @author novo
 * @date 2021/11/7-20:24
 */
@Controller
@RequestMapping("/team")
@CrossOrigin
public class TeamController {
    @Autowired
    private TeamService teamService;
    /**
     * 分页查询队伍信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("/page")//Request URL: http://api.leyou.com/api/item/brand/page?key=&page=1&rows=5&sortBy=id&desc=false
    public ResponseEntity<PageResult<Team>> queryTeamsByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "page",required = false,defaultValue = "1") Integer page,//当前页码
            @RequestParam(value = "rows",required = false,defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,//排序字段
            @RequestParam(value = "desc",required = false) Boolean desc//是否降序
    ) {
        PageResult<Team> result=this.teamService.queryTeamsByPage(key,page,rows,sortBy,desc);
       if(CollectionUtils.isEmpty(result.getTeams())){
           //不用result==null 一般都要先判断为空 因为isEmpty源码没有判断为空 只判断了size==0
            // 因为我们queryBrandsByPage方法返回的对象是new出来的不可能为null 只需要判断内容为不为空
            return null;//404
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 删除队伍信息
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Integer id){
        JSONObject jsonObject=new JSONObject();
        Integer result = this.teamService.deleteTeam(id);
        if (result==null){
            jsonObject.put("message:","删除失败");
            return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
        }
        jsonObject.put("message:","删除成功");
        return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
    }


    /**
     * 上传队伍信息
     * @param username
     * @param phone
     * @param files
     * @return
     */
    @PostMapping("/save")
    @Transactional
    public ResponseEntity<String> saveTeam(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "phone") String phone,
            @RequestParam(value = "files") List<MultipartFile> files,
            HttpServletRequest request
    ){

        JSONObject result=new JSONObject();
        List<String> urls=new ArrayList<>();

        if(StringUtils.isBlank(username)||StringUtils.isBlank(phone)||phone.length()!=11) {
            result.put("message","请输入正确格式");
            return new ResponseEntity<>(result.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if(!CollectionUtils.isEmpty(files)){
            files.forEach(file -> {
                String url=this.teamService.upload(username,phone,file);
                if(StringUtils.isBlank(url)){
                    return;
                }
                urls.add(url);
            });
        }

        if(CollectionUtils.isEmpty(urls)){
            return ResponseEntity.badRequest().build();//参数不合法
        }
        Team team=this.teamService.saveTeam(username,phone,urls);
        if(team!=null){
            result.put("message","保存成功");
            return ResponseEntity.status(HttpStatus.CREATED).body(result.toJSONString());
        }
        result.put("message","保存失败");
        return new ResponseEntity<>(result.toJSONString(),HttpStatus.BAD_REQUEST);
    }
    /**
     * office在线预览
     * @param url
     * @param response
     * @throws Exception
     */
    @RequestMapping("/onlinePreview")
    public ResponseEntity<String> onlinePreview(@RequestParam("url") String url, HttpServletResponse response) throws Exception{
        String msg = this.teamService.onlinePreview(url, response);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("message",msg);
        return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);

    }

    /**
     * 单文件下载
     * @param response
     * @param url
     * @return
     * @throws IOException
     */
    @GetMapping("/downloadOne")
    public ResponseEntity<String> downloadOne(HttpServletResponse response, @RequestParam("url") String url) throws IOException {
        JSONObject jsonObject = new JSONObject();
        File file1 = new File(url);
        HttpServletResponse httpServletResponse = downloadZip(file1, response);
        if(httpServletResponse==null){
            jsonObject.put("message","下载的文件不存在");
            return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
        }
        jsonObject.put("message","下载成功");
        // this.teamService.downloadOne(response, url);
        return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
    }
    /**
     * 文件打包下载
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("/downloadAll")
    public ResponseEntity<String> down(HttpServletResponse response, @RequestBody List<String> urls) throws Exception {
        JSONObject jsonObject = new JSONObject();
        List<File> files = new ArrayList<>();
        urls.forEach(url->{
            System.out.println(url);
            File file = new File(url);
            files.add(file);
        });

     /*   File file2 = new File("E:\\Project\\2021年第十六届结构设计大赛报名表（兴趣组.xlsx");
        files.add(file2);*/
        HttpServletResponse httpServletResponse = downLoadFiles(files, response);
        if (httpServletResponse==null){
            jsonObject.put("message","下载失败");
            return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
        }
        jsonObject.put("message","下载成功");
        return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
    }

    @GetMapping("/downloadMax")
    public ResponseEntity<String> downloadMax(HttpServletResponse response) throws Exception {
        List<String> urls=this.teamService.selectAllUrls();
        JSONObject jsonObject = new JSONObject();
        List<File> files = new ArrayList<>();
        urls.forEach(url->{
            url=url.split("\"")[1];
            File file = new File(url);
            files.add(file);
        });

     /*   File file2 = new File("E:\\Project\\2021年第十六届结构设计大赛报名表（兴趣组.xlsx");
        files.add(file2);*/
        HttpServletResponse httpServletResponse = downLoadFiles(files, response);
        if (httpServletResponse==null){
            jsonObject.put("message","下载失败");
            return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
        }
        jsonObject.put("message","下载成功");
        return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.CREATED);
    }
    @GetMapping("/getAll")
    public ResponseEntity<List<Team>> getAllData(){
        return ResponseEntity.ok(this.teamService.selectAll());
    }
}
