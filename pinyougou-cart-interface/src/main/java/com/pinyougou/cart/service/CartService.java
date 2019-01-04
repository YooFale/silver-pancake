package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

/**
 * 购物车服务接口
 * 
 * @author yoofale
 *
 */
public interface CartService {
	/**
	 * 添加商品到购物车列表
	 * @param list
	 * @param itemId 明细id
	 * @param num 商品数量
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
	/**
	 * 从Redis中提取购物车
	 * @param suername
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	/**
	 * 将购物车列表存入Redis
	 * @param username
	 * @param cartList
	 * @return
	 */
	public void saveCartListToRedis(String username,List<Cart> cartList);
	/**
	 * 合并购物车(只负责合并的逻辑)
	 * @param cartList1
	 * @param cartList2
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
