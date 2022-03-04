package com.kirin.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.kirin.miaosha.domain.MiaoshaUser;

@Mapper
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id = #{id}")
	public MiaoshaUser getById(@Param("id")long id);

	//修改密码：MiaoshaUserService.java / updatePassword()
	@Update("update miaosha_user set password = #{password} where id = #{id}")
	public void update(MiaoshaUser toBeUpdate);
}
