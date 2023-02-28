package com.wyu.controller;

import com.wyu.common.PageResult;
import com.wyu.common.Resp;
import com.wyu.model.Team;
import com.wyu.service.TeamService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.util.List;


/**
 * @author novo
 * @date 2021/11/7-20:24
 */
@RestController
@RequestMapping("/team")
@CrossOrigin
@Validated
public class TeamController {
    @Autowired
    private TeamService teamService;

    @ApiOperation("查询")
    @GetMapping("/page")
    public Resp queryTeamsByPage(
            @ApiParam("模糊搜索关键字") @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,//当前页码
            @RequestParam(value = "page_size", required = false, defaultValue = "5") Integer pageSize,
            @ApiParam("排序字段") @RequestParam(value = "sortBy", required = false) String sortBy,//排序字段
            @ApiParam("是否降序") @RequestParam(value = "desc", required = false) Boolean desc) {
        PageResult<Team> result = this.teamService.queryTeamsByPage(key, page, pageSize, sortBy, desc);
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
            @RequestPart(value = "file") @NotNull MultipartFile file) {
        if (file.isEmpty()) {
            return Resp.error("文件不能为空");
        }
        String url = this.teamService.upload(username, phone, file);
        Team team = this.teamService.saveTeam(username, phone, url);
        return Resp.success(team);
    }

    @ApiImplicitParam(name="ids", value="用户id", required=true, paramType="query" ,allowMultiple=true, dataTypeClass = Integer.class)
    @PostMapping("/download")
    public void download(HttpServletResponse response, @RequestParam(required = false) List<Integer> ids, @ApiParam("是否下载全部 是的话ids可以不传") @RequestParam(required = false, defaultValue = "false") Boolean all) {
        this.teamService.download(response, ids, all);
    }

    @GetMapping("/getAll")
    public Resp getAllData() {
        return Resp.success(this.teamService.selectAll());
    }
}
