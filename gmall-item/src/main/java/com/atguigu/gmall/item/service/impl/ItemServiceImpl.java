package com.atguigu.gmall.item.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.service.to.ProductAllInfos;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Service(version = "1.0")
public class ItemServiceImpl implements ItemService{

    @Reference
    SkuStockService skuStockService;
    @Reference
    ProductService productService;

    @Override
    public ProductAllInfos getInfo(Long skuId) {
        ProductAllInfos infos = new ProductAllInfos();
        //1.当前sku的详细信息查出来：包括销售属性的组合。库存，价格
        SkuStock skuStock = skuStockService.getById(skuId);

        //2.当前商品的详细xinx
        Long productId = skuStock.getProductId();
        //引入缓存机制
        //1.查询商品，直接去缓存种查询
        //如果缓存中没有，去数据库查询。查来的数据再放入缓存，下一个人就不用查了；
        Product product = productService.getProductByIdFromCache(productId);

        //3.所有sku的组合选法
        List<SkuStock> skuStocks = skuStockService.getAllSkuInfoByProductId(productId);
        //4.优惠劵查询
        List<EsProductAttributeValue> saleAttr = productService.getProductSaleAttr(productId);
        //5.当前商品涉及到的服务
        List<EsProductAttributeValue> baseAttr = productService.getProductBaseAttr(productId);
        //6.当前商品涉及到的服务

        infos.setSaleAttr(saleAttr);
        infos.setBaseAttr(baseAttr);
        infos.setSkuStock(skuStock);
        infos.setSkuStocks(skuStocks);
        infos.setProduct(product);

        return infos;
    }
}
