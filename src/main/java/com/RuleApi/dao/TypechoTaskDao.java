package com.RuleApi.dao;

import com.RuleApi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * dao层接口
 * TypechoUserlogDao
 * @author buxia97
 * @date 2022/01/06
 */
@Mapper
public interface TypechoTaskDao {

    /**
     * [新增]
     **/
    int insert(TypechoTask typechoTask);

    /**
     * [批量新增]
     **/
    int batchInsert(List<TypechoTask> list);

    /**
     * [更新]
     **/
    int update(TypechoTask typechoTask);

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
    TypechoTask selectByKey(Object key);

    /**
     * [条件查询]
     **/
    List<TypechoTask> selectList (TypechoTask typechoTask);

    /**
     * [分页条件查询]
     **/
    List<TypechoTask> selectPage (@Param("typechoTask") TypechoTask typechoTask, @Param("page") Integer page, @Param("pageSize") Integer pageSize);

    /**
     * [总量查询]
     **/
    int total(TypechoTask typechoTask);

}
