package com.novo.common;

import java.util.List;

/**
 * @author novo
 * @date 2021/10/28-14:58
 */
public class PageResult<T> {
    private List<T> teams;
    private Long total;
    private Integer totalPage;

    public PageResult() {
    }

    public PageResult(List<T> teams, Long total, Integer totalPage) {
        this.teams = teams;
        this.total = total;
        this.totalPage = totalPage;
    }

    public PageResult(List<T> teams, Long total) {
        this.teams = teams;
        this.total = total;
    }

    public List<T> getTeams() {
        return teams;
    }

    public void setTeams(List<T> teams) {
        this.teams = teams;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
}
