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
 * TypechoSpaceServiceImpl
 * @author buxia97
 * @date 2023/02/05
 */
@Service
public class SpaceServiceImpl implements SpaceService {

	@Autowired
	SpaceDao dao;

	@Override
	public int insert(Space space) {
		return dao.insert(space);
	}

	@Override
	public int batchInsert(List<Space> list) {
		return dao.batchInsert(list);
	}

	@Override
	public int update(Space space) {
		return dao.update(space);
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
	public Space selectByKey(Object key) {
		return dao.selectByKey(key);
	}

	@Override
	public List<Space> selectList(Space space) {
		return dao.selectList(space);
	}

	@Override
	public PageList<Space> selectPage(Space space, Integer offset, Integer pageSize, String order, String searchKey, Integer isReply) {
		PageList<Space> pageList = new PageList<>();

		int total = this.total(space,searchKey);

		Integer totalPage;
		if (total % pageSize != 0) {
			totalPage = (total /pageSize) + 1;
		} else {
			totalPage = total /pageSize;
		}

		int page = (offset - 1) * pageSize;

		List<Space> list = dao.selectPage(space, page, pageSize,order,searchKey,isReply);

		pageList.setList(list);
		pageList.setStartPageNo(offset);
		pageList.setPageSize(pageSize);
		pageList.setTotalCount(total);
		pageList.setTotalPageCount(totalPage);
		return pageList;
	}

	@Override
	public int total(Space space, String searchKey) {
		return dao.total(space,searchKey);
	}
}