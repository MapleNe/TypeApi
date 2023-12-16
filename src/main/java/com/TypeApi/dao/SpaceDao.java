package com.TypeApi.dao;

import com.TypeApi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * dao层接口
 * TypechoSpaceDao
 * @author buxia97
 * @date 2023/02/05
 */
@Mapper
public interface SpaceDao {

    /**
     * [新增]
     **/
    int insert(Space space);

    /**
     * [批量新增]
     **/
    int batchInsert(List<Space> list);

    /**
     * [更新]
     **/
    int update(Space space);

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
    Space selectByKey(Object key);

    /**
     * [条件查询]
     **/
    List<Space> selectList (Space space);

    /**
     * [分页条件查询]
     **/
    List<Space> selectPage (@Param("space") Space space, @Param("page") Integer page, @Param("pageSize") Integer pageSize, @Param("order") String order, @Param("searchKey") String searchKey, @Param("isReply") Integer isReply);

    /**
     * [总量查询]
     **/
    int total(@Param("space") Space space, @Param("searchKey") String searchKey);
}
