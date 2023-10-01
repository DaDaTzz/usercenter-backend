package com.da.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新标签请求封装类
 */
@Data
public class UpdateTagRequest implements Serializable {
    private static final long serialVersionUID = -5313786860637852175L;

    /**
     * 标签数组
     */
    private String[] tags;
}
