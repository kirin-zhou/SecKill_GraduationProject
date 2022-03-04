package com.kirin.miaosha.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kirin.miaosha.dao.GoodsDao;
import com.kirin.miaosha.domain.MiaoshaGoods;
import com.kirin.miaosha.vo.GoodsVo;

@Service
public class GoodsService {
	
	@Autowired
	GoodsDao goodsDao;
	
	//1.查询所有的商品：实现联表查询,将商品表和秒杀商品表的数据拼到一起
	public List<GoodsVo> listGoodsVo(){
		return goodsDao.listGoodsVo();
	}

	//2.根据id获取商品
	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	//3.减少库存：秒杀
	public boolean reduceStock(GoodsVo goods) {
		MiaoshaGoods g = new MiaoshaGoods();
		g.setGoodsId(goods.getId());
		int ret = goodsDao.reduceStock(g);
		return ret > 0;
	}
	
	//4.还原库存
	public void resetStock(List<GoodsVo> goodsList) {
		for(GoodsVo goods : goodsList ) {
			MiaoshaGoods g = new MiaoshaGoods();
			g.setGoodsId(goods.getId());
			g.setStockCount(goods.getStockCount());
			goodsDao.resetStock(g);
		}
	}
}