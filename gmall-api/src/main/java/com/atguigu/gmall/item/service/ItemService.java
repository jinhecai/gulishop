package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.service.to.ProductAllInfos;

public interface ItemService {
    ProductAllInfos getInfo(Long skuId);
}
