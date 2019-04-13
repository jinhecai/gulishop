package com.atguigu.gmall.portal.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.service.to.ProductAllInfos;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/item ")
public class ItemController {

    @Reference
    ItemService itemService;

    //如果返回的数据可能 跟约定的不符；
    //1.SpringMvc会觉得什么后缀的请求，就会返回什么数据类型的结果，如果不匹配，直接报错
    @GetMapping(value = "/{skuId}.html",produces = "application/json")
    public String productInfo(@PathVariable("skuId")Long skuId){
        //
        ProductAllInfos allInfos =itemService.getInfo(skuId);
        return "ok";
    }
}
