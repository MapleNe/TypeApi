package com.TypeApi.service.impl;

import com.TypeApi.entity.*;
import com.TypeApi.common.PageList;
import com.TypeApi.dao.*;
import com.TypeApi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务层实现类
 * TypechoFieldsServiceImpl
 * @author buxia97
 * @date 2021/11/29
 */
@Service
public class FieldsServiceImpl implements FieldsService {

    @Autowired
	FieldsDao dao;

    @Override
    public int insert(Fields fields) {
        return dao.insert(fields);
    }

    @Override
    public int batchInsert(List<Fields> list) {
    	return dao.batchInsert(list);
    }

    @Override
    public int update(Fields fields) {
    	return dao.update(fields);
    }

    @Override
    public int delete(Integer cid,String name) {
    	return dao.delete(cid,name);
    }

    @Override
    public int batchDelete(List<Object> keys) {
        return dao.batchDelete(keys);
    }

	@Override
	public List<Fields> selectByKey(Object key) {
		return dao.selectByKey(key);
	}

	@Override
	public List<Fields> selectList(Fields fields) {
		return dao.selectList(fields);
	}

	@Override
	public PageList<Fields> selectPage(Fields fields, Integer offset, Integer pageSize) {
		PageList<Fields> pageList = new PageList<>();

		int total = this.total(fields);

		Integer totalPage;
		if (total % pageSize != 0) {
			totalPage = (total /pageSize) + 1;
		} else {
			totalPage = total /pageSize;
		}

		int page = (offset - 1) * pageSize;

		List<Fields> list = dao.selectPage(fields, page, pageSize);

		pageList.setList(list);
		pageList.setStartPageNo(offset);
		pageList.setPageSize(pageSize);
		pageList.setTotalCount(total);
		pageList.setTotalPageCount(totalPage);
		return pageList;
	}

	@Override
	public int total(Fields fields) {
		return dao.total(fields);
	}
}