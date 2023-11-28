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
 * TypechoContentsServiceImpl
 *
 * @author buxia97
 * @date 2021/11/29
 */
@Service
public class TypechoHeadpictureServiceImpl implements TypechoHeadpictureService {

    @Autowired
    TypechoHeadpictureDao dao;

    @Override
    public int insert(TypechoHeadpicture TypechoHeadpicture) {
        return dao.insert(TypechoHeadpicture);
    }

    @Override
    public int batchInsert(List<TypechoHeadpicture> list) {
        return dao.batchInsert(list);
    }

    @Override
    public int update(TypechoHeadpicture TypechoHeadpicture) {
        return dao.update(TypechoHeadpicture);
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
    public TypechoHeadpicture selectByKey(Object key) { return dao.selectByKey(key);}

    @Override
    public List<TypechoHeadpicture> selectList(TypechoHeadpicture TypechoHeadpicture) {
        return dao.selectList(TypechoHeadpicture);
    }

    @Override
    public PageList<TypechoHeadpicture> selectPage(TypechoHeadpicture TypechoHeadpicture, Integer offset, Integer pageSize, String order) {
        PageList<TypechoHeadpicture> pageList = new PageList<>();

        int total = this.total(TypechoHeadpicture);

        Integer totalPage;
        if (total % pageSize != 0) {
            totalPage = (total / pageSize) + 1;
        } else {
            totalPage = total / pageSize;
        }

        int page = (offset - 1) * pageSize;

        List<TypechoHeadpicture> list = dao.selectPage(TypechoHeadpicture, page, pageSize, order);

        pageList.setList(list);
        pageList.setStartPageNo(offset);
        pageList.setPageSize(pageSize);
        pageList.setTotalCount(total);
        pageList.setTotalPageCount(totalPage);
        return pageList;
    }

    @Override
    public int total(TypechoHeadpicture TypechoHeadpicture) {
        return dao.total(TypechoHeadpicture);
    }
}