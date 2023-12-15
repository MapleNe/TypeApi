package com.RuleApi.dao;

import com.RuleApi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 * Dao层
 * 订单
 * Author Maplene
 */

@Mapper
public interface TypechoOrderDao {

    /**
     * [新增]
     **/
    int insert(TypechoOrder TypechoOrder);

    /**
     * [批量新增]
     **/
    int batchInsert(List<TypechoOrder> list);

    /**
     * [更新]
     **/
    int update(TypechoOrder TypechoOrder);

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
    TypechoOrder selectByKey(Object key);

    /**
     * [订单查询]
     **/
    TypechoOrder selectByOrder(Object orders);

    /**
     * [条件查询]
     **/
    List<TypechoOrder> selectList(TypechoOrder TypechoOrder);

    /**
     * [分页条件查询]
     **/
    List<TypechoOrder> selectPage(@Param("TypechoOrder") TypechoOrder TypechoOrder, @Param("page") Integer page, @Param("pageSize") Integer pageSize, @Param("searchKey") String searchKey, @Param("order") String order);

    /**
     * [总量查询]
     **/
    int total(TypechoOrder TypechoOrder);
}
