package com.RuleApi.service.impl;

import com.RuleApi.entity.*;
import com.RuleApi.common.PageList;
import com.RuleApi.dao.*;
import com.RuleApi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 业务层实现类
 * TypechoUserlogServiceImpl
 * @author buxia97
 * @date 2022/01/06
 */
@Service
public class TypechoTaskServiceImpl implements TypechoTaskService {

    @Autowired
    TypechoTaskDao dao;

    @Override
    public int insert(TypechoTask typechoTask) {
        return dao.insert(typechoTask);
    }


    @Override
    public int batchInsert(List<TypechoTask> list) {
        return dao.batchInsert(list);
    }

    @Override
    public int update(TypechoTask typechoTask) {
        return dao.update(typechoTask);
    }

    @Override
    public int delete(Object key) {
        return dao.delete(key);
    }

    @Override
    public int batchDelete(List<Object> keys) {
        return dao.batchDelete(keys);
    }

    @Override
    public TypechoTask selectByKey(Object key) {
        return dao.selectByKey(key);
    }

    @Override
    public List<TypechoTask> selectList(TypechoTask typechoTask) {
        return dao.selectList(typechoTask);
    }

    @Override
    public PageList<TypechoTask> selectPage(TypechoTask typechoTask, Integer offset, Integer pageSize) {
        PageList<TypechoTask> pageList = new PageList<>();

        int total = this.total(typechoTask);

        Integer totalPage;
        if (total % pageSize != 0) {
            totalPage = (total /pageSize) + 1;
        } else {
            totalPage = total /pageSize;
        }

        int page = (offset - 1) * pageSize;

        List<TypechoTask> list = dao.selectPage(typechoTask, page, pageSize);

        pageList.setList(list);
        pageList.setStartPageNo(offset);
        pageList.setPageSize(pageSize);
        pageList.setTotalCount(total);
        pageList.setTotalPageCount(totalPage);
        return pageList;
    }

    @Override
    public int total(TypechoTask typechoTask) {
        return dao.total(typechoTask);
    }
}