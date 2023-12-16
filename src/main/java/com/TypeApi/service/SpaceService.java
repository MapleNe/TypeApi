package com.TypeApi.service;

import java.util.List;
import com.TypeApi.entity.*;
import com.TypeApi.common.PageList;

/**
 * 业务层
 * TypechoSpaceService
 * @author buxia97
 * @date 2023/02/05
 */
public interface SpaceService {

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
    int batchDelete(List<Object> keys);

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
    PageList<Space> selectPage (Space space, Integer page, Integer pageSize, String order, String searchKey, Integer isReply);

    /**
     * [总量查询]
     **/
    int total(Space space, String searchKey);
}
