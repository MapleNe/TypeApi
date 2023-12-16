package com.TypeApi.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * TypechoHeadpicture
 *
 * @author Maplene 2023/11/20
 */
@Data
public class Headpicture implements Serializable {
    private static final long serialVersionUID = 1L;

    /***
     * Integer id
     ***/
    private Integer id;

    /***
     * String name
     ***/
    private String name;

    /***
     * String link
     ***/
    private String link;

    /***
     * Integer Status
     ***/
    private Integer Status;

    /***
     * Integer type
     ***/
    private Integer type;

    /***
     * String permission
     ***/
    private String permission;

    /***
     * Integer creator
     ***/
    private Integer creator;
}
