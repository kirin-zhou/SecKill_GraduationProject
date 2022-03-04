package com.kirin.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kirin.miaosha.dao.UserDao;
import com.kirin.miaosha.domain.User;

@Service
public class UserService {
	
	//U1-4 数据库测试1：获取id
	@Autowired
	UserDao userDao;
	
	public User getById(int id) {
		return userDao.getById(id);
	}

	//U1-4 数据库测试2：事务（插入数据）
	@Transactional  //事务标签
	public boolean tx() {
		//插入数据id=2
		User u1 = new User();
		u1.setId(2);
		u1.setName("2222");
		userDao.insert(u1);
		
		//插入数据id=1，希望报错
		User u2 = new User();
		u2.setId(1);
		u2.setName("1111");
		userDao.insert(u2);
		
		return true;
	}
}
