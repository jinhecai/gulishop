package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.atguigu.gmall.pms.vo.PmsProductAttributeParam;
import com.atguigu.gmall.pms.vo.ProductAttrInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品属性参数表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
public interface ProductAttributeService extends IService<ProductAttribute> {

    boolean updateProductAttributeById(Long id, PmsProductAttributeParam productAttributeParam);
    //分页查询销售属性或者基本属性
    Map<String,Object> selectProductAttributeByCategory(Long cid, Integer type, Integer pageNum, Integer pageSize);


    boolean saveProductAttribute(PmsProductAttributeParam productAttributeParam);


    List<ProductAttrInfo> getAttrInfoList(Long productCategoryId);
}
