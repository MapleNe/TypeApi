package com.RuleApi.service.impl;

import com.RuleApi.entity.*;
import com.RuleApi.common.PageList;
import com.RuleApi.dao.*;
import com.RuleApi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TypechoOrderServiceImpl implements TypechoOrderService {

    @Autowired
    TypechoOrderDao dao;

    @Override
    public int insert(TypechoOrder TypechoOrder) {
        return dao.insert(TypechoOrder);
    }

    @Override
    public int batchInsert(List<TypechoOrder> list) {
        return dao.batchInsert(list);
    }

    @Override
    public int update(TypechoOrder TypechoOrder) {
        return dao.update(TypechoOrder);
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
    public TypechoOrder selectByKey(Object key) {
        return dao.selectByKey(key);
    }

    @Override
    public TypechoOrder selectByOrder(Object orders) {
        return dao.selectByOrder(orders);
    }

    @Override
    public List<TypechoOrder> selectList(TypechoOrder TypechoOrder) {
        return dao.selectList(TypechoOrder);
    }

    @Override
    public PageList<TypechoOrder> selectPage(TypechoOrder TypechoOrder, Integer offset, Integer pageSize, String searchKey, String order) {
        PageList<TypechoOrder> pageList = new PageList<>();

        int total = this.total(TypechoOrder);

        Integer totalPage;
        if (total % pageSize != 0) {
            totalPage = (total /pageSize) + 1;
        } else {
            totalPage = total /pageSize;
        }

        int page = (offset - 1) * pageSize;

        List<TypechoOrder> list = dao.selectPage(TypechoOrder, page, pageSize,searchKey,order);

        pageList.setList(list);
        pageList.setStartPageNo(offset);
        pageList.setPageSize(pageSize);
        pageList.setTotalCount(total);
        pageList.setTotalPageCount(totalPage);
        return pageList;
    }

    @Override
    public int total(TypechoOrder TypechoOrder) {
        return dao.total(TypechoOrder);
    }
}
