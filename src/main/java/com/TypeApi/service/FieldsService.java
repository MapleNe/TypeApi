package com.TypeApi.service;

import java.util.List;
import com.TypeApi.entity.*;
import com.TypeApi.common.PageList;

/**
 * 业务层
 * TypechoFieldsService
 * @author buxia97
 * @date 2021/11/29
 */
public interface FieldsService {

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
    int delete(Integer cid, String name);

    /**
     * [批量删除]
     **/
    int batchDelete(List<Object> keys);

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
    PageList<Fields> selectPage (Fields fields, Integer page, Integer pageSize);

    /**
     * [总量查询]
     **/
    int total(Fields fields);
}
