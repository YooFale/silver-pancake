package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * 
 * @author yoofale
 *
 */
public interface BrandService {
	public List<TbBrand> findAll();

	/**
	 * 品牌分页
	 * 
	 * @param pageNum当前页码
	 * @param pageSize每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);

	/**
	 * 查询分页(重载)
	 * 
	 * @param brand
	 *            要查询的条件
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);

	/**
	 * 增加品牌
	 */
	public void add(TbBrand brand);

	/**
	 * 查询到要修改的对象
	 * 
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);

	/**
	 * 将修改后的结果存回去
	 * 
	 * @param brand
	 */
	public void update(TbBrand brand);

	/**
	 * 删除
	 * 
	 * @param ids
	 */
	public void delete(long[] ids);
}
