package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	@Autowired
	private IdWorker idWorker;
	
	@Override
		public void submitOrder(Long seckillId, String userId) {
//			1.查询缓存中的商品
			TbSeckillGoods seckillGoods =  (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			if(seckillGoods==null){
						throw new RuntimeException("商品不存在!");
					}
			if(seckillGoods.getStockCount()<=0){
				throw new RuntimeException("商品已被抢购完毕!");
			}
			
//			2.减少库存
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);//减库存
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);//将抢购后的状态存入缓存
			if(seckillGoods.getStockCount()==0){
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//将库存为0的状态更新到数据库
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
				System.out.println("将商品同步到数据库");
			}
			
//			3.存储秒杀订单(因为未付款,所以不向数据库存储,只向缓存中存储)
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(idWorker.nextId());//生成订单编号
			seckillOrder.setSeckillId(seckillId);//秒杀的商品id
			seckillOrder.setMoney(seckillGoods.getCostPrice());
			seckillOrder.setUserId(userId);
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			seckillOrder.setCreateTime(new Date());//下单日期
			seckillOrder.setStatus("0");//状态(可以不存储,在完成订单时候直接赋值)
			
			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);//将用户id和订单属性存入缓存中
			System.out.println("订单已经保存到redis中");
		}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		// 从缓存中提取订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder == null) {
			throw new RuntimeException("不存在该订单!");
		}
		if (seckillOrder.getId().longValue() != orderId.longValue()) {
			throw new RuntimeException("订单号不符~!");
		}
		
		//修改订单实体的属性
		seckillOrder.setPayTime(new Date());
		seckillOrder.setStatus("1");
		seckillOrder.setTransactionId(transactionId);
		
//		将订单存入数据库
		seckillOrderMapper.insert(seckillOrder);
//		清除缓存中的订单信息
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
//		1.查询出缓存中的订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder!=null){
			//删除缓存
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
		}
		
//		2.库存的回退
		TbSeckillGoods seckillGoods =(TbSeckillGoods) redisTemplate.boundHashOps("seckillOrder").get(seckillOrder.getSeckillId());
		if(seckillGoods!=null){
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);//库存量+1
			redisTemplate.boundHashOps("seckillOrder").put(seckillOrder.getSeckillId(), seckillGoods);//将最新的库存写回redis
		}else{
			seckillGoods = new TbSeckillGoods();
			seckillGoods.setId(seckillOrder.getSeckillId());
			//属性设置省略
			seckillGoods.setStockCount(1);//数量设置为1
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
		}
		System.out.println("订单取消:"+orderId);
	}

}
