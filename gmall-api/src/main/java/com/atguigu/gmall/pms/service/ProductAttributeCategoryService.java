package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductAttributeCategory;
import com.atguigu.gmall.pms.vo.PmsProductAttributeCategoryItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品属性分类表 服务类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
public interface ProductAttributeCategoryService extends IService<ProductAttributeCategory> {


    Map<String, Object> pageProductAttributeCategoryInfo(Integer pageSize, Integer pageNum);

    void saveName(String name);

    void updateProductAttributeCategoryById(Long id, String name);

    List<PmsProductAttributeCategoryItem> getProductAttributeCategoryAndItem();
}
