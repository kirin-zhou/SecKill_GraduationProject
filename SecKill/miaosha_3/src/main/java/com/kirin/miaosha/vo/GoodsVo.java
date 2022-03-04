package com.kirin.miaosha.vo;

import java.util.Date;
import com.kirin.miaosha.domain.Goods;

//实现联表查询：将商品表和秒杀商品表的数据拼到一起，输出商品列表页
//创建一个GoodsVo类，继承Goods类，再将秒杀商品表中特有的字段，添加进去
public class GoodsVo extends Goods{
	private Double miaoshaPrice;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
	
	public Integer getStockCount() {
		return stockCount;
	}
	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Double getMiaoshaPrice() {
		return miaoshaPrice;
	}
	public void setMiaoshaPrice(Double miaoshaPrice) {
		this.miaoshaPrice = miaoshaPrice;
	}
}
