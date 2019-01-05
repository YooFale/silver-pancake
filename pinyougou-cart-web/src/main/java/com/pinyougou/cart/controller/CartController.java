package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference(timeout = 6000)
	private CartService cartService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;

	/**
	 * 从Cookie中提取购物车
	 * 
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		//获取当前登录的账号
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录的账号:"+username);
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		if(username.equals("anonymousUser")){//如果未登录
			System.out.println("从Cookie中提取购物车");
			
			return cartList_cookie;
		}else{//如果已登录
			//获取redis中的购物车
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			
			if(cartList_cookie.size()>0){
				//合并购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_redis, cartList_cookie);
				//将合并后的购物车存入redis
				cartService.saveCartListToRedis(username, cartList);
				//清除Cookie中的本地购物车
				util.CookieUtil.deleteCookie(request, response, "cartList");
				System.out.println("将购物车合并");
				return cartList;
			}
			
			return cartList_redis;
		}
	}

	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId, Integer num) {
		/*
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//可以访问的域
		//如果需要涉及对Cookie的操作(允许携带凭证Cookie,如果允许携带Cookie,可以访问的域不能是通配符)
		response.setHeader("Access-Control-Allow-Credentials", "true");
		*/
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录名:"+username);
		
		try {
			// 从Cookie中提取到购物车
			List<Cart> cartList = findCartList();
			// 调用服务方法操作购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if(username.equals("anonymousUser")){//如果未登录
				// 将操作后的购物车装回Cookie
				String cartListString = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
				System.out.println("向Cookie中存储购物车");
			}else{
				cartService.saveCartListToRedis(username, cartList);
			}
		
			return new Result(true, "存入购物车成功");
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "存入购物车失败");
		}
	}
}
