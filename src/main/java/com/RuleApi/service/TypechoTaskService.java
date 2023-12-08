package com.RuleApi.service;

import java.util.Map;
import java.util.List;
import com.RuleApi.entity.*;
import com.RuleApi.common.PageList;

/**
 * 业务层
 * TypechoUserlogService
 * @author buxia97
 * @date 2022/01/06
 */
public interface TypechoTaskService {

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
    int batchDelete(List<Object> keys);

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
    PageList<TypechoTask> selectPage (TypechoTask typechoTask, Integer page, Integer pageSize);

    /**
     * [总量查询]
     **/
    int total(TypechoTask typechoTask);

}
