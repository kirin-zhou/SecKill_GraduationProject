package com.kirin.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kirin.miaosha.dao.MiaoshaUserDao;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.exception.GlobalException;
import com.kirin.miaosha.redis.MiaoshaUserKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.util.MD5Util;
import com.kirin.miaosha.util.UUIDUtil;
import com.kirin.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
	
	public static final String COOKI_NAME_TOKEN = "token";
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService; //为了用token标识用户，要将用户信息写入Redis缓存中
	
	public MiaoshaUser getById(long id) {
		return miaoshaUserDao.getById(id);
	}
	

	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword(); //获取数据库中的密码
		String saltDB = user.getSalt(); //获取数据库中的静态salt
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB); //做2次MD5
		if(!calcPass.equals(dbPass)) { //判断计算出来的MD5加密密码和数据库中的密码是否一致
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//登录成功后，生成cookie
		String token = UUIDUtil.uuid();
		addCookie(response, token, user);
		return true;
	}

	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		//首次登陆的时候，需要将Cookie存入Redis：token与user对应，获得token就能知道user信息
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token); //name:value = COOKI_NAME_TOKEN:token
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds()); //设置cookie的有效期 = MiaoshaUserKey.token的有效期
		cookie.setPath("/"); //设置为根目录，则可以在整个应用范围内使用cookie
		response.addCookie(cookie);
	}

	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		//参数校验
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
}

