package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.atguigu.gmall.pms.entity.ProductAttributeCategory;
import com.atguigu.gmall.pms.mapper.ProductAttributeCategoryMapper;
import com.atguigu.gmall.pms.mapper.ProductAttributeMapper;
import com.atguigu.gmall.pms.service.ProductAttributeCategoryService;
import com.atguigu.gmall.pms.vo.PmsProductAttributeCategoryItem;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品属性分类表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
@Service
@Component
public class ProductAttributeCategoryServiceImpl extends ServiceImpl<ProductAttributeCategoryMapper, ProductAttributeCategory> implements ProductAttributeCategoryService {
    @Autowired
    ProductAttributeMapper productAttributeMapper;
    @Override
    public Map<String, Object> pageProductAttributeCategoryInfo(Integer pageSize, Integer pageNum) {
        ProductAttributeCategoryMapper baseMapper = getBaseMapper();

        Page<ProductAttributeCategory> page = new Page<>(pageNum, pageSize);

        IPage<ProductAttributeCategory> selectPage = baseMapper.selectPage(page, null);
        //封装数据
        Map<String, Object> map = new HashMap<>();
        map.put("pageSize",pageSize);
        map.put("totalPage",selectPage.getPages());
        map.put("total",selectPage.getTotal());
        map.put("pageNum",selectPage.getCurrent());
        map.put("list",selectPage.getRecords());

        return map;
    }

    @Override
    public void saveName(String name) {
        ProductAttributeCategoryMapper baseMapper = getBaseMapper();
        ProductAttributeCategory productAttributeCategory = new ProductAttributeCategory();
        productAttributeCategory.setName(name);
        baseMapper.insert(productAttributeCategory);
    }

    @Override
    public void updateProductAttributeCategoryById(Long id, String name) {
        ProductAttributeCategoryMapper baseMapper = getBaseMapper();
        ProductAttributeCategory productAttributeCategory = new ProductAttributeCategory();
        productAttributeCategory.setName(name);
        productAttributeCategory.setId(id);
        baseMapper.updateById(productAttributeCategory);
    }

    //获取所有商品属性分类及其下属性
    @Override
    public List<PmsProductAttributeCategoryItem> getProductAttributeCategoryAndItem() {
        ArrayList<PmsProductAttributeCategoryItem> pmsProductAttributeCategoryItems = new ArrayList<>();
        ProductAttributeCategoryMapper baseMapper = getBaseMapper();
        List<ProductAttributeCategory> categoryList = baseMapper.selectList(null);
        categoryList.forEach(productAttributeCategory -> {
            PmsProductAttributeCategoryItem pmsProductAttributeCategoryItem = new PmsProductAttributeCategoryItem();
            BeanUtils.copyProperties(productAttributeCategory,pmsProductAttributeCategoryItem);
            Long id = pmsProductAttributeCategoryItem.getId();
            List<ProductAttribute> productAttributes = productAttributeMapper.selectProductId(id);
            pmsProductAttributeCategoryItem.setProductAttributeList(productAttributes);
            pmsProductAttributeCategoryItems.add(pmsProductAttributeCategoryItem);
        });

        return pmsProductAttributeCategoryItems;
    }


}
