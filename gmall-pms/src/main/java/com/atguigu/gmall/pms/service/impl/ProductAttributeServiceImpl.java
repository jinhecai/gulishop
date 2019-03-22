package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.atguigu.gmall.pms.mapper.ProductAttributeMapper;
import com.atguigu.gmall.pms.service.ProductAttributeService;
import com.atguigu.gmall.pms.vo.PmsProductAttributeParam;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品属性参数表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
@Service
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeMapper, ProductAttribute> implements ProductAttributeService {

    @Override
    public boolean updateProductAttributeById(Long id, PmsProductAttributeParam productAttributeParam) {
        ProductAttribute productAttribute = new ProductAttribute();
        BeanUtils.copyProperties(id,productAttributeParam);
        productAttribute.setId(id);

        Integer result = baseMapper.updateById(productAttribute);

        return null != result && result>0;
    }
}
