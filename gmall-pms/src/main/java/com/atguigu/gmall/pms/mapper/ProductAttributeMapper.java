package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 商品属性参数表 Mapper 接口
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
public interface ProductAttributeMapper extends BaseMapper<ProductAttribute> {

    List<ProductAttribute> selectProductId(Long id);
}
