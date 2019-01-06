package com.pinyougou.cart.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference
	private WeixinPayService weixinPayService;
	
	
	@RequestMapping("/createNative")
	public Map createNative(){ 
		IdWorker idWorker = new IdWorker();
		return weixinPayService.createNative(idWorker.nextId()+"","1");
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		Result result =null;
		int x=0;
		while(true){
			Map map = weixinPayService.queryPayStatus(out_trade_no);
			if(map==null){
				result =new Result(false, "支付发生错误");
				break;
			}
			if(map.get("trade_state").equals("SUCCESS")){
				result =new Result(true,"支付成功");
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x++;
			if(x>=100){
				result = new Result(false,"支付超时!");
				break;
			}
		}
		return result;
	}
	
	
}
