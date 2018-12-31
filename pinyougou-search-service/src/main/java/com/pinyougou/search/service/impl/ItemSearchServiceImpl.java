package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap<>();
		//关键字去空格
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		/*
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		
		map.put("rows", page.getContent());
		*/
		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.分组查询商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		//3.查询品牌和规格列表
		String category = (String) searchMap.get("category");
		if("".equals(category)){
			map.putAll(searchBrandAndSpecList(category));
		}else{
			if(categoryList.size()>0){
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		return map;
	}
	
	//查询列表
	private Map searchList(Map searchMap){
		Map map = new HashMap<>();
		//高亮选项的初始化
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//为高亮设置前缀
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2按分类筛选
		if(!"".equals(searchMap.get("category"))){			
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.3按品牌筛选
		if (!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.4按规格过滤
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap = (Map<String,String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		//1.5按价格区间过滤
			if (!"".equals(searchMap.get("price"))) {
				String[] price = ((String)searchMap.get("price")).split("-");
				if(!price[0].equals("0")){//区间最低不为0
					Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
					FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
				if(!price[1].equals("*")){//区间最高不为*
					Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
					FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
			}
		//1.6分页
			Integer pageNo = (Integer) searchMap.get("pageNo");//从前端获取到的页码
			if(pageNo==null){
				pageNo=1;
			}
			Integer pageSize = (Integer) searchMap.get("pageSize");//从前端获取到的每页记录数
			if(pageSize==null){
				pageSize=20;
			}
			
			query.setOffset(pageSize*(pageNo-1));//设置起始索引
			query.setRows(pageSize);//设置每页记录数
			
		}
		//1.7按价格排序
		String sortValue = (String) searchMap.get("sort");//获取排序方式
		String sortField = (String) searchMap.get("sortField");//获取排序字段
		
		if(sortValue!=null && !sortField.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}



		
		//*****************获取高亮结果集*******************
		// 返回高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();//获得高亮结果
		for (HighlightEntry<TbItem> entry : entryList) {
			//获取高亮列表(取决于高亮域的个数)
			List<Highlight> highlightList = entry.getHighlights();
			/*
			for (Highlight highlight : highlightList) {
				List<String> sns = highlight.getSnipplets();//因为每个域可能存储多个值
				System.out.println(sns);
			}
			*/
			// 为了程序健壮性,需要做一下判断
			// getEntity和下方的 getContent指向同一个地方
			if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0){
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		return map;
	}
	
	/**
	 * 分组查询
	 * @return
	 */
	private List<String> searchCategoryList(Map searchMap){
		List<String> list = new ArrayList<String>();
		
		Query query = new SimpleQuery("*:*");
		//根据关键字查询:相当于sql中的where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//设置分组选项:相当于分组-groupby
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions );
		//获取分组页page.getContent不可能获得值.
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
		//获取分组结果对象可能获得多个值,因为可能有多个分组.上面可以一直addGroupByField
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		
		for (GroupEntry<TbItem> entry : entryList) {
			list.add(entry.getGroupValue());//将分组的结果添加到返回值中
		}
		return list;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 根据商品分类名称查询品牌和规格列表
	 * @param category商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap();
		//1.根据商品分类名称得到模板Id
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId!=null){
			//2.根据模板Id获取品牌列表
			List brnadList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brnadList);
			
			//3.根据模板Id获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
		}
		return map;
		
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);	
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIds) {
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
	}
	
}
