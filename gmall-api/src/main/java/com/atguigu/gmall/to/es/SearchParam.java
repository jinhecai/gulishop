package com.atguigu.gmall.to.es;

import lombok.Data;

import java.io.Serializable;

//检索前端传递的数据
@Data
public class SearchParam implements Serializable {

    private String[] catelog3Id;//三级分类id

    private String[] brandId;//品牌id

    private String keyword;//检索的关键字

    private String order;//0:综合排序 1：销量  2：价格  3：价格区间

    private Integer pageNum = 1;//分页信息

    //props=1:小米&props=2:全高清
    private String[] props;//页面提交的数组

    private Integer pageSize = 12;

    private Integer priceFrom;//价格区间开始
    private Integer priceTo;//价格区间结束

}
