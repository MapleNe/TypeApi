package com.RuleApi.service;
import java.util.List;
import com.RuleApi.entity.*;
import com.RuleApi.common.PageList;

/**
 * 业务层
 * TypechoHeadpictureSerice
 * @author Maplene
 * @date 2023/11/20
 */

public interface TypechoHeadpictureService {

    int insert(TypechoHeadpicture TypechoHeadpicture);

    int batchInsert(List<TypechoHeadpicture> list);

    int update(TypechoHeadpicture TypechoHeadpicture);

    int delete(Object key);

    int batchDelete(List<Object> keys);

    TypechoHeadpicture selectByKey(Object key);

    List<TypechoHeadpicture> selectList(TypechoHeadpicture TypechoHeadpicture);

    PageList<TypechoHeadpicture> selectPage(TypechoHeadpicture TypechoHeadpicture, Integer offset, Integer pageSize,String order);

    int total(TypechoHeadpicture TypechoHeadpicture);
}
