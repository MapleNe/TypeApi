package com.RuleApi.dao;

import com.RuleApi.entity.*;
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
public interface TypechoHeadpictureDao {

    /**
     * [新增]
     **/
    int insert(TypechoHeadpicture TypechoHeadpicture);

    /**
     * [批量新增]
     **/
    int batchInsert(List<TypechoHeadpicture> list);

    /**
     * [更新]
     **/
    int update(TypechoHeadpicture TypechoHeadpicture);

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
    TypechoHeadpicture selectByKey(Object key);

    /**
     * [条件查询]
     **/
    List<TypechoHeadpicture> selectList (TypechoHeadpicture TypechoHeadpicture);

    /**
     * [分页条件查询]
     **/
    List<TypechoHeadpicture> selectPage (@Param("TypechoHeadpicture") TypechoHeadpicture TypechoHeadpicture, @Param("page") Integer page, @Param("pageSize") Integer pageSize, @Param("order") String order);

    /**
     * [总量查询]
     **/
    int total(TypechoHeadpicture TypechoHeadpicture);

}
