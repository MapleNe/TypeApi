package com.TypeApi.dao;

import com.TypeApi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * dao层接口
 * TypechoHeadpicture
 * @author Maplene
 * @date 2023/11/20
 */
@Mapper
public interface HeadpictureDao {

    /**
     * [新增]
     **/
    int insert(Headpicture Headpicture);

    /**
     * [批量新增]
     **/
    int batchInsert(List<Headpicture> list);

    /**
     * [更新]
     **/
    int update(Headpicture Headpicture);

    /**
     * [删除]
     **/
    int delete(Object key);

    /**
     * [批量删除]
     **/
    int batchDelete(List<Object> list);

    /**
     * [主键查询]
     **/
    Headpicture selectByKey(Object key);

    /**
     * [条件查询]
     **/
    List<Headpicture> selectList (Headpicture Headpicture);

    /**
     * [分页条件查询]
     **/
    List<Headpicture> selectPage (@Param("Headpicture") Headpicture Headpicture, @Param("page") Integer page, @Param("pageSize") Integer pageSize, @Param("order") String order);

    /**
     * [总量查询]
     **/
    int total(Headpicture Headpicture);

}
