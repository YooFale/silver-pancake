package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {
	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		// 1.根据SKU的ID查询商品明细的对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("商品不存在");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}
		// 2.根据SKU对象得到商家Id
		String sellerId = item.getSellerId();
		
		// 3.根据商家ID在购物车列表中查询购物车对象
		Cart cart = searchCartBySellerId(cartList, sellerId);
		
		if (cart == null) { // 4.购物车列表中不存在该商家的购物车项

			// 4.1 创建一个新的购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			ArrayList<TbOrderItem> orderItemList = new ArrayList<>();// 创建购物车明细列表
			TbOrderItem orderItem = createOrderItem(item, num);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);

			// 4.2 将新的购物车项添加到购物车中
			cartList.add(cart);

		} else {// 5.如果购物车列表中存在该商家的购物车项
			// 判断该商品是否在该购物车的明细列表中存在
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if(orderItem==null){// 5.1 如果购物车不存在该商品,创建新的购物车项
				orderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(orderItem);
			}else{// 5.2 如果购物车项存在该商品,数量+num
				orderItem.setNum(orderItem.getNum()+num);//更改数量
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));//获取金额
				//当明细的数量小于等于0,移除该明细
				if(orderItem.getNum()<=0){
					cart.getOrderItemList().remove(orderItem);
				}
				//当购物车的明细列表数量为0,在购物车列表中移除此购物车
				if(cart.getOrderItemList().size()==0){
					cartList.remove(cart);
				}
			}
			
		}
		return cartList;
	}

	/**
	 * 根据商家ID在购物车列表中查询购物车对象
	 * 
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	/**
	 * 根据SKUID在购物车明细列表中查询购物车明细对象
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
		for (TbOrderItem orderItem : orderItemList) {
			if(orderItem.getItemId().longValue()==itemId.longValue()){
				return orderItem;
			}
		}
		return null;
	}
	/**
	 * 创建购物车明细对象
	 * 
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item, Integer num) {
		//创建新的购物车明细对象
				TbOrderItem orderItem=new TbOrderItem();
				orderItem.setGoodsId(item.getGoodsId());
				orderItem.setItemId(item.getId());
				orderItem.setNum(num);
				orderItem.setPicPath(item.getImage());
				orderItem.setPrice(item.getPrice());
				orderItem.setSellerId(item.getSellerId());
				orderItem.setTitle(item.getTitle());
				orderItem.setTotalFee(  new BigDecimal(item.getPrice().doubleValue()*num) );
				return orderItem;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中获取到购物车"+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null){
			cartList = new ArrayList();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis中存入购物车"+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		// 简单合并,同名商家不合并,同名商品不合并
		//		cartList1.addAll(cartList2);
		for (Cart cart : cartList2) {
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				cartList1 = addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
			}
		}
		return cartList1;
	}
}
