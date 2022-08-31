package com.example.ssmdemo.support;


import lombok.Data;

import java.util.List;

@Data
public class Pager<T> {

    /**
     * 当前页
     */
    private int currentPage = 1;

    /**
     * 每页条数
     */
    private int pageSize = 10;

    /**
     * 总页数
     */
    private int pageCount;

    /**
     * 总条数
     */
    private int totalCount;

    private List<T> data;

    public Pager() {
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.calPageCount();
    }

    public void calPageCount() {
        this.pageCount = (int) Math.ceil((this.totalCount * 1.0) / this.pageSize);
    }

}
