package com.pinyougou.page.service.impl;

import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;
@Service
public class ItemPageServiceImpl implements ItemPageService {
	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		
		Configuration configuration = freeMarkerConfigurer.getConfiguration();
		
		try {
			Template template = configuration.getTemplate("item.ftl");
			//创建数据模型
			Map datamodel = new HashMap();
			//1.商品主表数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			datamodel.put("goods", goods);
			//2.商品扩展表数据
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			datamodel.put("goodsDesc", goodsDesc);
			//3.读取商品分类
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			datamodel.put("itemCat1", itemCat1);
			datamodel.put("itemCat2", itemCat2);
			datamodel.put("itemCat3", itemCat3);
			
			//4.读取SKU列表
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			
			criteria.andGoodsIdEqualTo(goodsId);//匹配SPU的id
			criteria.andStatusEqualTo("1");//确保状态有效
			example.setOrderByClause("is_default desc");//按照是否默认字段进行降序排序,确保返回的结果第一条为默认的SKU
			
			List<TbItem> itemList = itemMapper.selectByExample(example );
			datamodel.put("itemList", itemList);
			
			Writer out = new FileWriter(pagedir+goodsId+".html");
			template.process(datamodel,out);//输出
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

}
