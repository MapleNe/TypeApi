package com.TypeApi.dao;

import com.TypeApi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * dao层接口
 * TypechoFieldsDao
 * @author buxia97
 * @date 2021/11/29
 */
@Mapper
public interface FieldsDao {

    /**
     * [新增]
     **/
    int insert(Fields fields);

    /**
     * [批量新增]
     **/
    int batchInsert(List<Fields> list);

    /**
     * [更新]
     **/
    int update(Fields fields);

    /**
     * [删除]
     **/
    int delete(Integer cid,String name);

    /**
     * [批量删除]
     **/
    int batchDelete(List<Object> list);

    /**
     * [主键查询]
     **/
    List<Fields> selectByKey(Object key);

    /**
     * [条件查询]
     **/
    List<Fields> selectList (Fields fields);

    /**
     * [分页条件查询]
     **/
    List<Fields> selectPage (@Param("fields") Fields fields, @Param("page") Integer page, @Param("pageSize") Integer pageSize);

    /**
     * [总量查询]
     **/
    int total(Fields fields);
}
