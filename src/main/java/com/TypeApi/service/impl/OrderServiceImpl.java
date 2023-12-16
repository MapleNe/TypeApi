package com.TypeApi.service.impl;

import com.TypeApi.entity.*;
import com.TypeApi.common.PageList;
import com.TypeApi.dao.*;
import com.TypeApi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDao dao;

    @Override
    public int insert(Order Order) {
        return dao.insert(Order);
    }

    @Override
    public int batchInsert(List<Order> list) {
        return dao.batchInsert(list);
    }

    @Override
    public int update(Order Order) {
        return dao.update(Order);
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
    public Order selectByKey(Object key) {
        return dao.selectByKey(key);
    }

    @Override
    public Order selectByOrder(Object orders) {
        return dao.selectByOrder(orders);
    }

    @Override
    public List<Order> selectList(Order Order) {
        return dao.selectList(Order);
    }

    @Override
    public PageList<Order> selectPage(Order Order, Integer offset, Integer pageSize, String searchKey, String order) {
        PageList<Order> pageList = new PageList<>();

        int total = this.total(Order);

        Integer totalPage;
        if (total % pageSize != 0) {
            totalPage = (total /pageSize) + 1;
        } else {
            totalPage = total /pageSize;
        }

        int page = (offset - 1) * pageSize;

        List<Order> list = dao.selectPage(Order, page, pageSize,searchKey,order);

        pageList.setList(list);
        pageList.setStartPageNo(offset);
        pageList.setPageSize(pageSize);
        pageList.setTotalCount(total);
        pageList.setTotalPageCount(totalPage);
        return pageList;
    }

    @Override
    public int total(Order Order) {
        return dao.total(Order);
    }
}
