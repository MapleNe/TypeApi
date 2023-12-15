package com.RuleApi.service;
import java.util.Map;
import java.util.List;
import com.RuleApi.entity.*;
import com.RuleApi.common.PageList;
public interface TypechoOrderService {
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
    int batchDelete(List<Object> keys);

    /**
     * [主键查询]
     **/
    TypechoOrder selectByKey(Object key);

    /**
     * [orders查询]
     **/
    TypechoOrder selectByOrder(Object orders);

    /**
     * [条件查询]
     **/
    List<TypechoOrder> selectList (TypechoOrder TypechoOrder);

    /**
     * [分页条件查询]
     **/
    PageList<TypechoOrder> selectPage (TypechoOrder TypechoOrder, Integer page, Integer pageSize,String searchKey,String order);

    /**
     * [总量查询]
     **/
    int total(TypechoOrder TypechoOrder);
}
