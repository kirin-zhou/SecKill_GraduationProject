package com.kirin.miaosha.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.kirin.miaosha.domain.MiaoshaGoods;
import com.kirin.miaosha.vo.GoodsVo;

@Mapper
public interface GoodsDao {
	
	//1.查询所有的商品：实现联表查询，将商品表和秒杀商品表的数据拼到一起
	@Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id")
	public List<GoodsVo> listGoodsVo();

	//2.根据id获取商品
	@Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
	public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

	//3.减少库存：秒杀
	//添加stock_count>0的条件：当多个线程同时读取到同一个库存数量时，防止超卖
	@Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
	public int reduceStock(MiaoshaGoods g);
}
