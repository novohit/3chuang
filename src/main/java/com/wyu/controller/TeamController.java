package com.wyu.controller;

import com.wyu.common.PageResult;
import com.wyu.common.Resp;
import com.wyu.model.Team;
import com.wyu.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import java.util.List;


/**
 * @author wyu
 * @date 2021/11/7-20:24
 */
@RestController
@RequestMapping("/team")
@CrossOrigin
@Validated
public class TeamController {
    @Autowired
    private TeamService teamService;

    @GetMapping("/page")
    public Resp queryTeamsByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,//当前页码
            @RequestParam(value = "rows", required = false, defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,//排序字段
            @RequestParam(value = "desc", required = false) Boolean desc) {
        PageResult<Team> result = this.teamService.queryTeamsByPage(key, page, rows, sortBy, desc);
        return Resp.success(result);
    }

    @DeleteMapping("/delete/{id}")
    public Resp deleteTeam(@PathVariable Integer id) {
        this.teamService.deleteTeam(id);
        return Resp.success();
    }

    @PostMapping("/save")
    @Transactional
    public Resp saveTeam(
            @RequestParam(value = "username") @NotBlank(message = "姓名不能为空") String username,
            @RequestParam(value = "phone") @Pattern(regexp = "^[1][3,4,5,6,7,8,9][0-9]{9}$", message = "手机号格式有误") String phone,
            @RequestParam(value = "file") MultipartFile file) {
        if (file.isEmpty()) {
            return Resp.error("文件不能为空");
        }
        String url = this.teamService.upload(username, phone, file);
        Team team = this.teamService.saveTeam(username, phone, url);
        return Resp.success(team);
    }

    @PostMapping("/download")
    public void download(HttpServletResponse response, @RequestParam List<Integer> ids, @RequestParam(required = false, defaultValue = "false") Boolean all) {
        this.teamService.download(response, ids, all);
    }

    @GetMapping("/getAll")
    public Resp getAllData() {
        return Resp.success(this.teamService.selectAll());
    }
}
