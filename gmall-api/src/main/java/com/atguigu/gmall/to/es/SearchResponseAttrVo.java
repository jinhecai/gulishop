package com.atguigu.gmall.to.es;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponseAttrVo {


    private Long productAttributeId;
    //当前属性值的所有值
    private List<String> value = new ArrayList<>();
    //属性名称
    private String name;//网络制式
}
