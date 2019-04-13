package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.mapper.SkuStockMapper;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * sku的库存 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-03-19
 */
@Service
@Component
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {
    @Autowired
    SkuStockMapper skuStockMapper;

    @Override
    public List<SkuStock> getAllSkuInfoByProductId(Long productId) {

        return  skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id",productId));
    }

    @Override
    public List<SkuStock> getSkuStock(Long pid,String keyword) {

        List<SkuStock> skuStocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().like("sku_code",keyword).eq("product_id",pid));

        return skuStocks;
    }

    @Override
    public boolean updateSkuStock(Long pid, List<SkuStock> skuStockList) {
        for (SkuStock skuStock : skuStockList) {
            QueryWrapper<SkuStock> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",skuStock.getId()).eq("product_id",skuStock.getProductId());
            Integer result = baseMapper.update(skuStock, queryWrapper);
            if(!(null!=result && result>0)){
                return false;
            }
        }
        return true;

    }

    @Override
    public BigDecimal getSkuPriceById(Long skuId) {
        //TODO 查缓存+读写锁
        SkuStock skuStock = skuStockMapper.selectById(skuId);
        return skuStock.getPrice();
    }

}
