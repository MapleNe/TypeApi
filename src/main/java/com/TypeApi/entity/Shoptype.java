package com.TypeApi.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * TypechoShoptype
 * @author shoptype 2023-07-10
 */
@Data
public class Shoptype implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id  
     */
    private Integer id;

    /**
     * parent  上级分类
     */
    private Integer parent;

    /**
     * name  分类名称
     */
    private String name;

    /**
     * pic  分类缩略图
     */
    private String pic;

    /**
     * intro  分类简介
     */
    private String intro;

    /**
     * orderKey  分类排序
     */
    private Integer orderKey;
}