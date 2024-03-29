package com.TypeApi.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * TypechoInvitation
 * @author invitation 2022-05-03
 */
@Data
public class Invitation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id  
     */
    private Integer id;

    /**
     * code  邀请码
     */
    private String code;

    /**
     * created  创建时间
     */
    private Integer created;

    /**
     * uid  创建者
     */
    private Integer uid;

    /**
     * status  0未使用，1已使用
     */
    private Integer status;
}