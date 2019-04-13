package com.atguigu.gmall.search;

import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.SearchParam;
import com.atguigu.gmall.to.es.SearchResponse;

import java.io.IOException;
import java.util.List;

public interface GmallSearchService {


    boolean saveProductInfoToEs(EsProduct esProduct);

    SearchResponse searchProduct(SearchParam param) throws IOException;
}
