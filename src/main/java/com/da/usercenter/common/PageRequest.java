package com.da.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -874299309843979195L;
    /**
     * 第几页
     */
    protected int pageNum = 1;
    /**
     * 页面记录条数
     */
    protected int pageSize = 10;

    


}
