package com.TypeApi.dao;

import com.TypeApi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 * Dao层
 * 订单
 * Author Maplene
 */

@Mapper
public interface OrderDao {

    /**
     * [新增]
     **/
    int insert(Order Order);

    /**
     * [批量新增]
     **/
    int batchInsert(List<Order> list);

    /**
     * [更新]
     **/
    int update(Order Order);

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
    Order selectByKey(Object key);

    /**
     * [订单查询]
     **/
    Order selectByOrder(Object orders);

    /**
     * [条件查询]
     **/
    List<Order> selectList(Order Order);

    /**
     * [分页条件查询]
     **/
    List<Order> selectPage(@Param("Order") Order Order, @Param("page") Integer page, @Param("pageSize") Integer pageSize, @Param("searchKey") String searchKey, @Param("order") String order);

    /**
     * [总量查询]
     **/
    int total(Order Order);
}
